[#ftl strict_vars=true]
[#--
/* Copyright (c) 2008, The Visigoth Software Society
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
 *     * Neither the name Visigoth Software Society, FreeCC, Sun Microsystems, Inc. 
 *       nor the names of any contributors may be used to endorse 
 *       or promote products derived from this software without specific prior written 
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
 --]
/* Generated by: ${generated_by}. Do not edit. ${filename} */
[#var classname = filename[0..(filename?length -6)]]
[#var package = ""]
[#if explicitPackageName??]
package ${explicitPackageName};
[#set package = explicitPackageName]
[#elseif grammar.nodePackage?has_content]
package ${grammar.nodePackage};
[#set package = grammar.nodePackage]
[/#if]

[#if package != grammar.parserPackage && grammar.parserPackage != ""]
import ${grammar.parserPackage}.*;
[/#if]

public class ${classname} extends ${grammar.baseNodeClassName} {
[#if grammar.options.nodeUsesParser]
    public ${classname}(${grammar.parserClassName} p, int id) {
        super(p, id);
    }

    public ${classname}(${grammar.parserClassName} p) {
        super(p, ${grammar.constantsClassName}.${classname?upper_case});
    }
[#else]
    public ${classname}(int id) {
        super(id);
    }

    public ${classname}() {
        super(${grammar.constantsClassName}.${classname?upper_case});
    }

[/#if]

[#if grammar.options.visitor]
   [#var RETURN_TYPE = grammar.options.visitorReturnType]
   [#if !RETURN_TYPE?has_content][#set RETURN_TYPE = "void"][/#if]
   [#var DATA_TYPE = grammar.options.visitorDataType]
   [#if !DATA_TYPE?has_content][#set DATA_TYPE="Object"][/#if]
   [#var THROWS = ""]
   [#if grammar.options.visitorException?has_content][#set THROWS = "throws " + grammar.options.visitorException][/#if]
	 public ${RETURN_TYPE} jjtAccept(${grammar.parserClassName}Visitor visitor, ${DATA_TYPE} data) ${THROWS} {
	   [#if RETURN_TYPE != "void"]
	     return visitor.visit(this, data);
	   [/#if]
   }      
[/#if]
}
