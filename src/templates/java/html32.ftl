[#ftl strict_vars=true]

[#var ONE_TABLE=true]
[#var title="BNF for "+grammar.parserClassName]
[#escape x as (x)?html]
[#macro page]
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<HTML>
  <HEAD>
  [#if grammar.options.CSS?has_content]
    <LINK REL="stylesheet" type="text/css" href="${grammar.options.CSS}"/>
  [/#if]
    <TITLE>${title}</TITLE>
  </HEAD>
  <BODY>
   <H1 ALIGN=CENTER>${title}</H1>
   [#nested][#t]
  </BODY>
</HTML>
[/#macro]

[#macro nonterminals]
   <H2 ALIGN=CENTER>NON-TERMINALS</H2>
   [#if ONE_TABLE]
      <TABLE>
   [/#if]
      [#nested]
   [#if ONE_TABLE]
      </TABLE>
   [/#if]
[/#macro]

[#macro tokenProductions]
  <H2 ALIGN=CENTER>TOKENS</H2>
  <TABLE>
    [#nested]
  </TABLE>
[/#macro]

[#macro tokenProduction]
  <TR><TD><PRE>[#t]
    [#nested][#t]
  </PRE></TD></TR>[#t]
[/#macro]

[#macro doRegexp re]

[/#macro]

[#macro production prod]
    [#if !ONE_TABLE]
      <TABLE ALIGN=CENTER>
      <CAPTION><STRONG>${prod.name}</STRONG></CAPTION>
    [/#if]
    <TR><TD><PRE>${prod.leadingComments}</PRE></TD></TR>
    <TR>
     <TD ALIGN=RIGHT VALIGN=BASELINE><A NAME="${utils.getID(prod.name)}">${prod.name}</A></TD>
     <TD ALIGN=CENTER VALIGN=BASELINE>::=</TD>
     <TD ALIGN=LEFT VALIGN=BASELINE>
      [#nested]
     </TD>
    </TR>
    [#if !ONE_TABLE]
      </TABLE>
      <HR>
    [/#if]
[/#macro]

[#macro nonTerminal nt]
   <A HREF="#${utils.getID(nt.name)}">${nt.name}</A>
[/#macro]


[#macro javacode prod]
  <I>java code</I>
[/#macro]

[/#escape]