/* Generated by: ${generated_by}. Do not edit. ${filename} */
[#if grammar.parserPackage?has_content]
package ${grammar.parserPackage};
[/#if]

/**
 * Token literal values and constants.
 */

public interface ${grammar.constantsClassName} {

  int EOF = 0; // End of file
  [#list grammar.orderedNamedTokens as regexp]
  int ${regexp.label} = ${regexp.ordinal};
  [/#list]
  
[#if !grammar.options.userDefinedLexer&&grammar.options.buildLexer]
  /**
   * Lexical States
   */
 [#list grammar.lexerData.lexicalStates as lexicalState]
  int ${lexicalState.name} = ${lexicalState_index};
 [/#list]
[/#if]

  String[] tokenImage = {
      "<EOF>",
    [#list grammar.allTokenProductions as tokenProduction]
      [#list tokenProduction.regexpSpecs as regexpSpec]
      [@output_regexp regexpSpec.regexp/][#rt]
      [#if tokenProduction_has_next || regexpSpec_has_next],[/#if][#lt]
      [/#list]
    [/#list]
  };


   String[] nodeNames = {
       "EOF", 
       [#list grammar.orderedNamedTokens as regexp]
          "${regexp.label}",
       [/#list]
       [#if grammar.options.treeBuildingEnabled]
           [#list grammar.nodeNames as name]
              "${name}", 
           [/#list]
       [/#if]
   };
}




[#macro output_regexp regexp]
   [#if regexp.class.name?ends_with("StringLiteral")]
      "\"${utils.addEscapes(utils.addEscapes(regexp.image))}\""   
   [#elseif regexp.label != ""]
      "<${regexp.label}>"
   [#else]
      "<token of kind ${regexp.ordinal}>"
   [/#if]
[/#macro]
