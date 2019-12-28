/* Copyright (c) 2008-2019 Jonathan Revusky, jon@revusky.com
 * Copyright (c) 2006, Sun Microsystems Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notices,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name Jonathan Revusky, FreeCC, Sun Microsystems, Inc.
 *       nor the names of any contributors may be used to endorse or promote
 *       products derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package freecc.output.java;

import java.io.*;
import java.util.*;
import freecc.lexgen.RegularExpression;

import freecc.*;
import freecc.parser.tree.CompilationUnit;

import freemarker.template.*;
import freemarker.cache.*;
import freemarker.ext.beans.BeansWrapper;

import freecc.parser.*;

public class FilesGenerator {

    private Configuration fmConfig;
    private Grammar grammar;
    private String currentFilename;
    private CodeInjector codeInjector;
    private Set<String> tokenSubclassFileNames = new HashSet<String>();

    void initializeTemplateEngine() throws IOException {
        fmConfig = new freemarker.template.Configuration();
        String filename = grammar.getFilename();
        File dir = new File(filename).getCanonicalFile().getParentFile();
        TemplateLoader templateLoader = new MultiTemplateLoader(new FileTemplateLoader(dir), 
                                                                new ClassTemplateLoader(this.getClass(), ""));
        fmConfig.setClassForTemplateLoading(FilesGenerator.class, "");
        fmConfig.setTemplateLoader(templateLoader);
        fmConfig.setObjectWrapper(new BeansWrapper());
        fmConfig.setNumberFormat("computer");
        fmConfig.setArithmeticEngine(freemarker.core.ast.ArithmeticEngine.CONSERVATIVE_ENGINE);
    }

    public FilesGenerator(Grammar grammar, List<Node> codeInjections) {
        this.grammar = grammar;
        this.codeInjector = new CodeInjector(grammar.getParserClassName(), 
                                             grammar.getLexerClassName(),
                                             grammar.getConstantsClassName(), 
                                             grammar.getBaseNodeClassName(),
                                             grammar.getParserPackage(), 
                                             grammar.getNodePackage(), 
                                             codeInjections);
    }

    public void generateAll() throws IOException, TemplateException, MetaParseException {
        if (grammar.getErrorCount() != 0) {
            throw new MetaParseException();
        }
        initializeTemplateEngine();
//        System.out.println("Outputting parser source in directory: " + grammar.getParserOutputDirectory());
        generateLexicalException();
        generateParseException();
        generateToken();
        generateLexer();
        generateCharStream();
        generateConstantsFile();
        generateParser();
        generateDoc();
        generateNodeFile();
        generateUtilsFile();
        if (grammar.getOptions().getTreeBuildingEnabled()) {
            generateTreeBuildingFiles();
        }
    }
    
    public void generate(File outputFile) throws IOException, TemplateException  {
        this.currentFilename = outputFile.getName();
        String templateName = currentFilename + ".ftl";
        if (tokenSubclassFileNames.contains(currentFilename)) {
            templateName = "ASTToken.java.ftl";
        }
        else if (currentFilename.equals(grammar.getParserClassName() + ".java")) {
            templateName = "Parser.java.ftl";
        }
        else if (currentFilename.equals(grammar.getConstantsClassName() + ".java")) {
            templateName = "Constants.java.ftl";
        }
        else if (currentFilename.endsWith("Lexer.java")
                 || currentFilename.equals(grammar.getLexerClassName() + ".java")) {
            templateName = "Lexer.java.ftl";
        }
        else if (currentFilename.endsWith("CharStream.java")) {
            templateName = "CharStream.java.ftl";
        }
        else if (currentFilename.endsWith(".html")) {
            templateName = "doc.html.ftl";
        }
        else if (currentFilename.endsWith("Visitor.java")) {
            templateName = "Visitor.java.ftl";
        }
        else if (currentFilename.equals(grammar.getBaseNodeClassName() + ".java")) {
            templateName = "BaseNode.java.ftl";
        }
        else if (currentFilename.startsWith(grammar.getOptions().getNodePrefix())) {
            if (!(currentFilename.equals("ParseException.java")
                    || currentFilename.equals("Token.java")
                    || currentFilename.equals("Node.java")
                    || currentFilename.equals("LexicalException.java")
                    || currentFilename.equals("Nodes.java")))
            {
                    templateName = "ASTNode.java.ftl";
            }
        }
        HashMap<String, Object> dataModel = new HashMap<String, Object>();
        dataModel.put("grammar", grammar);
        dataModel.put("filename", currentFilename);
        dataModel.put("utils", new Utils());
        dataModel.put("generated_by", "FreeCC " + Version.version);
        String classname = currentFilename.substring(0, currentFilename.length() - 5);
        if (codeInjector.getExplicitlyDeclaredPackage(classname) != null) {
            dataModel.put("explicitPackageName", codeInjector.getExplicitlyDeclaredPackage(classname));
        }
        Writer out = null;
        out = new StringWriter();
        Template template = fmConfig.getTemplate(templateName);
        template.process(dataModel, out);
        String code = out.toString();
        System.out.println("Outputting: " + outputFile);
        if (outputFile.getName().endsWith(".java")) {
            outputJavaFile(code, outputFile);
        } else {
            FileWriter outfile = new FileWriter(outputFile);
            try {
                outfile.write(code);
            } 
            finally {
                outfile.close();
            }
        }
    }
    
    void outputJavaFile(String code, File outputFile) throws IOException, TemplateException {
        FileWriter out = new FileWriter(outputFile);
        CompilationUnit jcu = null;
        try {
            jcu = FreeCCParser.parseJavaFile(new StringReader(code), outputFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                out.write(code);
            } finally {
                out.flush();
                out.close();
            }
            return;
        }
        codeInjector.injectCode(jcu);
        try {
            JavaFormatter formatter = new JavaFormatter();
            out.write(formatter.format(jcu));
        } finally {
            out.close();
        }
    }
    
    void generateCharStream() throws IOException, TemplateException {
        String filename = "CharStream.java";
        if (!grammar.getOptions().getUserCharStream() && !grammar.getOptions().getUserDefinedLexer()) {
            if (grammar.getOptions().getJavaUnicodeEscape()) {
                filename = "JavaCharStream.java";
            } else {
                filename = "SimpleCharStream.java";
            }
        }
        File outputFile = new File(grammar.getParserOutputDirectory(), filename);
        if (regenerate(outputFile)) {
            generate(outputFile);
        }
    }
    
    void generateConstantsFile() throws IOException, TemplateException {
        String filename = grammar.getConstantsClassName() + ".java";
        File outputFile = new File(grammar.getParserOutputDirectory(), filename);
        generate(outputFile);
    }

    void generateParseException() throws IOException, TemplateException {
        File outputFile = new File(grammar.getParserOutputDirectory(), "ParseException.java");
        if (regenerate(outputFile)) {
            generate(outputFile);
        }
    }

    void generateToken() throws IOException, TemplateException {
        File outputFile = new File(grammar.getParserOutputDirectory(), "Token.java");
        if (regenerate(outputFile)) {
            generate(outputFile);
        }
    }

    void generateLexicalException() throws IOException, TemplateException {
        File outputFile = new File(grammar.getParserOutputDirectory(), "LexicalException.java");
        if (regenerate(outputFile)) {
            generate(outputFile);
        }
    }

    void generateLexer() throws IOException, TemplateException {
        String filename = "Lexer.java";
        if (!grammar.getOptions().getUserDefinedLexer()) {
            filename = grammar.getLexerClassName() + ".java";
        }
        File outputFile = new File(grammar.getParserOutputDirectory(), filename);
        generate(outputFile);
    }
    
    void generateParser() throws MetaParseException, IOException, TemplateException {
        if (!grammar.getOptions().getBuildParser()) return;
        grammar.buildParserInfo();
        String filename = grammar.getParserClassName() + ".java";
        File outputFile = new File(grammar.getParserOutputDirectory(), filename);
        generate(outputFile);
    }
    
    void generateDoc() throws IOException, TemplateException {
        String filename = grammar.getParserClassName() + ".html";
        File outputFile = new File(grammar.getParserOutputDirectory(), filename);
        generate(outputFile);
    }
    
    void generateNodeFile() throws IOException, TemplateException {
        File outputFile = new File(grammar.getParserOutputDirectory(), "Node.java");
        if (regenerate(outputFile)) {
            generate(outputFile);
        }
    }
    
    void generateUtilsFile() throws IOException, TemplateException {
        File outputFile = new File(grammar.getParserOutputDirectory(), "Nodes.java");
        if (regenerate(outputFile)) {
            generate(outputFile);
        }
    }
    
    boolean regenerate(File file) {
        if (!file.exists()) return true;
        String filename = file.getName();
        if (filename.endsWith(".java")) {
            String typename = filename.substring(0, filename.length() -5);
            if (codeInjector.hasInjectedCode(typename)) {
                return true;
            }
        }
        return false;
    }
    
    void generateTreeBuildingFiles() throws IOException, TemplateException {
        Set<File> files = new LinkedHashSet<File>();
        files.add(getOutputFile(grammar.getBaseNodeClassName()));
        if (grammar.getOptions().getVisitor()) {
            files.add(getOutputFile(grammar.getParserClassName() + "Visitor"));
        }

        for (RegularExpression re : grammar.getOrderedNamedTokens()) {
            if (re.isPrivate()) continue;
            File outputFile = getOutputFile(re.getGeneratedClassName());
            files.add(outputFile);
            tokenSubclassFileNames.add(outputFile.getName());
        }
        for (String nodeName : grammar.getNodeNames()) {
            File outputFile = getOutputFile(nodeName);
            if (tokenSubclassFileNames.contains(outputFile.getName())) {
                String name = outputFile.getName();
                name = name.substring(0, name.length() -5);
                grammar.addSemanticError(null, "The name " + name + " is already used as a Token subclass.");
            }
            files.add(outputFile);
        }
        for (File file : files) {
            if (regenerate(file)) {
                generate(file);
            }
        }
    }
    
    // only used for tree-building files (a bit kludgy)
    private File getOutputFile(String nodeName) throws IOException {
        if (nodeName.equals(grammar.getBaseNodeClassName())) {
            return new File(grammar.getParserOutputDirectory(), nodeName + ".java");
        }
        String className = grammar.getNodeClassName(nodeName);
        //KLUDGE
        if (nodeName.equals(grammar.getParserClassName() + "Visitor") || nodeName.equals(grammar.getBaseNodeClassName())) {
            className = nodeName;
        }
        String explicitlyDeclaredPackage = codeInjector.getExplicitlyDeclaredPackage(className);
        if (explicitlyDeclaredPackage == null) {
            return new File(grammar.getNodeOutputDirectory(nodeName), className + ".java");            
        }
        String sourceBase = grammar.getOptions().getBaseSourceDirectory();
        if (sourceBase.equals("")) {
            return new File(grammar.getNodeOutputDirectory(nodeName), className + ".java");
        }
        return new File(new File(sourceBase, explicitlyDeclaredPackage.replace('.', '/')), className + ".java"); 
    }

    
    public class Utils {
        private Map<String, String> id_map = new HashMap<String, String>();
        private int id = 1;
        
        public String toHexString(int i) {
            return "0x" + Integer.toHexString(i);
        }

        public String toHexStringL(long l) {
            return "0x" + Long.toHexString(l) + "L";
        }

        public String toOctalString(int i) {
            return "\\" + Integer.toOctalString(i);
        }

        public int charAt(String s, int i) {
            return (int) s.charAt(i);
        }
        
        public String addEscapes(String input) {
            return FreeCCUtils.add_escapes(input);
        }

        public boolean isBitSet(long num, int bit) {
            return (num & (1L<<bit)) != 0;
        }
        
        public int firstCharAsInt(String s) {
            return (int) s.charAt(0);
        }
        
        public String powerOfTwoInHex(int i) {
            return toHexStringL(1L << i);
        }
        
        public BitSet newBitSet() {
            return new BitSet();
        }
        
        public String getID(String name) {
            String value = id_map.get(name);
            if (value == null) {
              value = "prod" + id++;
              id_map.put(name, value);
            }
            return value;
        }
    }
}
