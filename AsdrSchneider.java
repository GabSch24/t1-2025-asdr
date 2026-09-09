import java.io.*;

/*
Alguns comentários rápidos, apesar de eu (Gabriel Schneider) ter sido o único que escreveu código no repositório,
discuti assuntos da atividade com o Leo, o Wide, o Marcus e um pouco em aula com o Cadu.
Envio apenas no meu nome pois sou o único que não esteve na aula com a prova de 2014, mas queria deixar claro que não fiz 100% sozinho.
 */

public class AsdrSchneider {

  private static final int BASE_TOKEN_NUM = 301;
  
  public static final int IDENT  = 301;
  public static final int NUM 	 = 302;
  public static final int WHILE  = 303;
  public static final int IF	 = 304;
  public static final int ELSE = 305;
  public static final int INT	 = 306;
  public static final int DOUBLE	 = 307;
  public static final int BOOL	 = 308;
  public static final int FUNC	 = 309;
  public static final int VOID	 = 309;


  

    public static final String tokenList[] = 
      {"IDENT",
		 "NUM", 
		 "WHILE", 
		 "IF", 
		 "ELSE",
       "FUNC",
       "INT",
       "BOOL",
       "DOUBLE",
       "VOID"
      };
                                      
  /* referencia ao objeto Scanner gerado pelo JFLEX */
  private Yylex lexer;

  public ParserVal yylval;

  private static int laToken;
  private boolean debug;

  
  /* construtor da classe */
  public AsdrSchneider (Reader r) {
      lexer = new Yylex (r, this);
  }

  /***** Gramática original 
  Prog -->  ListaDecl

  ListaDecl -->  DeclVar  ListaDecl
              |  DeclFun  ListaDecl
              |  / vazio /

  DeclVar --> Tipo ListaIdent ';' DeclVar
            | / vazio /

  Tipo --> int | double | boolean

  ListaIdent --> IDENT , ListaIdent  
               | IDENT      

  DeclFun --> FUNC tipoOuVoid IDENT '(' FormalPar ')' '{' DeclVar ListaCmd '}' DeclFun
            | / vazio /

  TipoOuVoid --> Tipo | VOID

  FormalPar -> paramList | / vazio /

  paramList --> Tipo IDENT , ParamList
              | Tipo IDENT 

  Bloco --> { ListaCmd }

  ListaCmd --> Cmd ListaCmd
    |    / vazio /

  Cmd --> Bloco
      | while ( E ) Cmd
      | IDENT = E ;
      | if ( E ) Cmd RestoIf

  RestoIf -> else Cmd
        |    / vazio /

  E --> E + T
      | E - T
      | T

  T --> T * F
      | T / F
      | F    
      
  F -->  IDENT
      | NUM
      | ( E )

***/  

  /***** Gramática 'fatorada' 

  Prog -->  ListaDecl

  ListaDecl -->  DeclVar  ListaDecl
              |  DeclFun  ListaDecl
              |  / vazio /

  DeclVar --> Tipo ListaIdent ';' DeclVar
            | / vazio /

  Tipo --> int | double | boolean

  ListaIdent --> IDENT , ListaIdent
               | IDENT        

  DeclFun --> FUNC tipoOuVoid IDENT '(' FormalPar ')' '{' DeclVar ListaCmd '}' DeclFun
            | / vazio /

  TipoOuVoid --> Tipo | VOID

  FormalPar -> paramList | / vazio /

  paramList --> Tipo IDENT , ParamList
              | Tipo IDENT 

  Bloco --> { ListaCmd }

  ListaCmd --> Cmd ListaCmd
    |    / vazio /

  Cmd --> Bloco
      | while ( E ) Cmd
      | IDENT = E ;
      | if ( E ) Cmd RestoIf

  RestoIf -> else Cmd
        |    / vazio /

 * fatorado: Eliminando a recursão direta à esquerda

  E --> T E'
  
  E'--> + T E'
      | - T E'
      | / vazio /

  T --> F T'

  T'--> * F T'
      | / F T'
      | / vazio / 

  F -->  IDENT
      | NUM
      | ( E )
***/ 

  private void Prog() {
      if (debug) System.out.println("Prog --> ListaDecl");
      ListaDecl();
   }

  private void ListaDecl() {
      if (laToken == FUNC) {
         if (debug) System.out.println("ListaDecl --> DeclFun");
         DeclFun();
         ListaDecl();
      }
      else if (laToken == INT || laToken == BOOL || laToken == DOUBLE)
      {
         if (debug) System.out.println("ListaDecl --> DeclVar");
         DeclVar();
         ListaDecl();
      }
      else 
      {
         // vazio
         if (debug) System.out.println("ListaDecl --> Vazio");
      }
   }

   private void DeclVar() {
      if (laToken == INT || laToken == BOOL || laToken == DOUBLE) {
         if (debug) System.out.println("DeclVar --> ListaIdent");
         Tipo();
         ListaIdent();
         verifica(';');
         DeclVar();
      }
      else
      {
         //vazio
         if (debug) System.out.println("DeclVar --> Vazio");
      }
  }

  private void Tipo() {
   switch (laToken)
      {
         case INT:
            verifica(INT);
            break;
         case BOOL:
            verifica(BOOL);
            break;
         case DOUBLE:
            verifica(DOUBLE);
            break;
         default:
            yyerror("Esperado INT ou BOOL ou DOUBLE");
            return;
      }
  }

  private void ListaIdent() {
      if (debug) System.out.printf("ListaIdent --> Ident");
      verifica(IDENT);
      while(laToken == ',')
      {
         if (debug) System.out.printf(", Ident");
         verifica(',');
         verifica(IDENT);
      }
      if (debug) System.out.println("");
  }

  private void DeclFun() {
      if (laToken == FUNC)
      {
         if (debug) System.out.println("DeclFun --> FUNC TipoOuVoid IDENT( FormalPar ) { DeclVar ListaCmd } DeclFun");
         verifica(FUNC);
         TipoOuVoid();
         verifica(IDENT);
         verifica('(');
         FormalPar();
         verifica(')');
         verifica('{');
         DeclVar();
         ListaCmd();
         verifica('}');
         DeclFun();
      }
      else
      {
         //vazio
         if (debug) System.out.println("DeclFun --> Vazio");
      }
  }

private void TipoOuVoid() {
   switch (laToken)
      {
         case INT:
            verifica(INT);
            break;
         case BOOL:
            verifica(BOOL);
            break;
         case DOUBLE:
            verifica(DOUBLE);
            break;
         case VOID:
            verifica(VOID);
            break;
         default:
            // só pra dar erro - arrumado pra usar yyerror
            yyerror("Esperado INT ou BOOL ou DOUBLE ou VOID");
            return;
      }
  }

  private void FormalPar() {
      if (laToken == INT || laToken == BOOL || laToken == DOUBLE) {
         if (debug) System.out.printf("FormalPar --> Tipo Ident");
         Tipo();
         verifica(IDENT);
         while(laToken == ',')
         {
            if (debug) System.out.printf(", Tipo Ident");
            verifica(',');
            Tipo();
            verifica(IDENT);
         }
         if (debug) System.out.println("");
      }
      else {
         // vazio
         if (debug) System.out.println("FormalPar --> Vazio");
      }
  }


  private void Bloco() {
      if (debug) System.out.println("Bloco --> { ListaCmd }");
      //if (laToken == '{') {
         verifica('{');
         ListaCmd();
         verifica('}');
      //}
  }

  private void ListaCmd() {
      // if (laToken == '{' || laToken == WHILE || laToken == IDENT || laToken == IF)
      // {
      //    Cmd();
      //    ListaCmd();
      // }
      // else
      // {
      //    //vazio
      // }
      while (laToken == '{' || laToken == WHILE || laToken == IDENT || laToken == IF)
      {
         if (debug) System.out.println("ListaCmd --> Cmd");
         Cmd();
      }
  }

  private void Cmd() {
      if (laToken == '{') {
         if (debug) System.out.println("Cmd --> Bloco");
         Bloco();
	   }    
      else if (laToken == WHILE) {
         if (debug) System.out.println("Cmd --> WHILE ( E ) Cmd");
         verifica(WHILE);    // laToken = this.yylex(); 
  		   verifica('(');
  		   E();
         verifica(')');
         Cmd();
	   }
      else if (laToken == IDENT ) {
         if (debug) System.out.println("Cmd --> IDENT = E ;");
            verifica(IDENT);  
            verifica('='); 
            E();
		      verifica(';');
	   }
    else if (laToken == IF) {
         if (debug) System.out.println("Cmd --> if (E) Cmd RestoIF");
         verifica(IF);
         verifica('(');
  		   E();
         verifica(')');
         Cmd();
         RestoIF();
	   }
   // Apenas agora vi que existe esse método e não precisava fazer com o verifica pra lançar erro...
 	else yyerror("Esperado {, if, while ou identificador");
   }


   private void RestoIF() {
       if (laToken == ELSE) {
         if (debug) System.out.println("RestoIF --> else Cmd");
         verifica(ELSE);
         Cmd();
    
	   } else {
         if (debug) System.out.println("RestoIF --> Vazio");
         }
     }     

   /*    E --> T E'
  
         E'-->   + T E'
               | - T E'
               | / vazio /
   */
   private void E() {
         if (laToken == IDENT || laToken == NUM || laToken == '(') {
            if (debug) System.out.println("E --> T E'");
            T();
            E_linha();
         }
         else yyerror("Esperado operando (, identificador ou numero");
      }

   private void E_linha() {
         if (laToken == '+' || laToken == '-') {
            if (debug) System.out.println("E --> T E'");
            switch (laToken)
            {
               case '+':
                  verifica('+');
                  T();
                  break;
               case '-':
                  verifica('-');
                  T();
                  break;
               default:
                  yyerror("Esperado + ou -");
                  return;
            }
            E_linha(); // É uma recursão na cauda então deve dar pra fazer em laço
         }
         else {
            if (debug) System.out.println("E' --> Vazio");
         }
      }

   /*
     T --> F T'

     T'--> * F T'
         | / F T'
         | / vazio / 
   */

   private void T() {
      if (laToken == IDENT || laToken == NUM || laToken == '(') {
            if (debug) System.out.println("T --> F T'");
            F();
            T_linha();
         }
         else yyerror("Esperado operando (, identificador ou numero");
   }

   private void T_linha(){
      if (laToken == '*' || laToken == '/') {
            if (debug) System.out.println("T' --> ");
            switch (laToken)
            {
               case '*':
                  verifica('*');
                  F();
                  break;
               case '/':
                  verifica('/');
                  F();
                  break;
               default:
                  yyerror("Esperado * ou /");
                  return;
            }
            T_linha(); // É uma recursão na cauda então deve dar pra fazer em laço
         }
      else {
         if (debug) System.out.println("T' --> Vazio");
      }
   }

   private void F() {
      switch (laToken)
      {
         case IDENT:
            if (debug) System.out.println("F --> IDENT");
            verifica(IDENT);
            break;
         case NUM:
            if (debug) System.out.println("F --> NUM");
            verifica(NUM);
            break;
         case '(':
            if (debug) System.out.println("F --> ( E )");
            verifica('(');
            E();
            verifica(')');
            break;
         default:
            yyerror("Esperado IDENT ou NUM ou ( E )");
            return;
      }
   }


  private void verifica(int expected) {
      if (laToken == expected)
         laToken = this.yylex();
      else {
         String expStr, laStr;       

		expStr = ((expected < BASE_TOKEN_NUM )
                ? ""+(char)expected
			     : tokenList[expected-BASE_TOKEN_NUM]);
         
		laStr = ((laToken < BASE_TOKEN_NUM )
                ? Character.toString(laToken)
                : tokenList[laToken-BASE_TOKEN_NUM]);

          yyerror( "esperado token: " + expStr +
                   " na entrada: " + laStr);
     }
   }

   /* metodo de acesso ao Scanner gerado pelo JFLEX */
   private int yylex() {
       int retVal = -1;
       try {
           yylval = new ParserVal(0); //zera o valor do token
           retVal = lexer.yylex(); //le a entrada do arquivo e retorna um token
       } catch (IOException e) {
           System.err.println("IO Error:" + e);
          }
       return retVal; //retorna o token para o Parser 
   }

  /* metodo de manipulacao de erros de sintaxe */
  public void yyerror (String error) {
     System.err.println("Erro: " + error);
     System.err.println("Entrada rejeitada");
     System.out.println("\n\nFalhou!!!");
     System.exit(1);
     
  }

  public void setDebug(boolean trace) {
      debug = trace;
  }


  /**
   * Runs the scanner on input files.
   *
   * This main method is the debugging routine for the scanner.
   * It prints debugging information about each returned token to
   * System.out until the end of file is reached, or an error occured.
   *
   * @param args   the command line, contains the filenames to run
   *               the scanner on.
   */
  public static void main(String[] args) {
     AsdrSchneider parser = null;
     try {
         if (args.length == 0)
            parser = new AsdrSchneider(new InputStreamReader(System.in));
         else 
            parser = new  AsdrSchneider( new java.io.FileReader(args[0]));

          parser.setDebug(false);
          laToken = parser.yylex();          

          parser.Prog();
     
          if (laToken== Yylex.YYEOF)
             System.out.println("\n\nSucesso!");
          else     
             System.out.println("\n\nFalhou - esperado EOF.");               

        }
        catch (java.io.FileNotFoundException e) {
          System.out.println("File not found : \""+args[0]+"\"");
        }
//        catch (java.io.IOException e) {
//          System.out.println("IO error scanning file \""+args[0]+"\"");
//          System.out.println(e);
//        }
//        catch (Exception e) {
//          System.out.println("Unexpected exception:");
//          e.printStackTrace();
//      }
    
  }
  
}


