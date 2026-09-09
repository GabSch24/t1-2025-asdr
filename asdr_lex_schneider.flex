%%

%{
  private AsdrSchneider yyparser;

  public Yylex(java.io.Reader r, AsdrSchneider yyparser) {
    this(r);
    this.yyparser = yyparser;
  }


%} 

%integer
%line
%char

WHITE_SPACE_CHAR=[\n\r\ \t\b\012]

%%

"$TRACE_ON"   { yyparser.setDebug(true); }
"$TRACE_OFF"  { yyparser.setDebug(false); }

"while"	 	{ return AsdrSchneider.WHILE; }
"if"		{ return AsdrSchneider.IF; }
"else"		{ return AsdrSchneider.ELSE; }
"int"		{ return AsdrSchneider.INT; }
"double"		{ return AsdrSchneider.DOUBLE; }
"boolean"		{ return AsdrSchneider.BOOL; }
"func"		{ return AsdrSchneider.FUNC; }
"void"    { return AsdrSchneider.VOID; }

[:jletter:][:jletterdigit:]* { return AsdrSchneider.IDENT; }  

[0-9]+(.[0-9]+)? 	{ return AsdrSchneider.NUM; }

"{" |
"}" |
";" |
"(" |
")" |
"+" |
"-" |
"*" |
"/" |
"," |
"="    	{ return yytext().charAt(0); } 


{WHITE_SPACE_CHAR}+ { }

. { System.out.println("Erro lexico: caracter invalido: <" + yytext() + ">"); }
