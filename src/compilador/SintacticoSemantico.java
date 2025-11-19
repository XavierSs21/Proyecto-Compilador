/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: ___________    HORA: ___________ HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintactico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   : Esta clase implementa un parser descendente del tipo 
 *:                 Predictivo Recursivo. Se forma por un metodo por cada simbolo
 *:                 No-Terminal de la gramatica mas el metodo emparejar ().
 *:                 El analisis empieza invocando al metodo del simbolo inicial.
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 22/Feb/2015 FGil                -Se mejoro errorEmparejar () para mostrar el
 *:                                 numero de linea en el codigo fuente donde 
 *:                                 ocurrio el error.
 *: 08/Sep/2015 FGil                -Se dejo lista para iniciar un nuevo analizador
 *:                                 sintactico.
 *: 20/FEB/2023 F.Gil, Oswi         -Se implementaron los procedures del parser
 *:                                  predictivo recursivo de leng BasicTec.
 *:-----------------------------------------------------------------------------
 */
package compilador;

import general.Linea_TS;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;

public class SintacticoSemantico {

    private Compilador cmp;
    private boolean analizarSemantica = false;
    private String preAnalisis;
    
    // --- Atributos para pasar información de tipo entre procedures (semántica)
    private String tipo_expresion;                 // atributo de expresion
    private String tipo_expresion_prima;           // atributo de expresion'
    private String tipo_expresion_simple;          // atributo de expresion_simple
    private String tipo_expresion_simple_prima;    // atributo de expresion_simple'
    private String tipo_termino;                   // atributo de termino
    private String tipo_termino_prima;             // atributo de termino'
    private String tipo_factor;                    // atributo de factor
    private String tipo_factor_prima;              // atributo de factor'

    private static final String VACIO = "VACIO";
    private static final String ERROR_TIPO = "ERROR_TIPO";
    private static final String ENTERO = "ENTERO";
    private static final String REAL = "REAL";
    private static final String VOID = "void";
    private static final String BOOLEANO = "BOOLEANO";

    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //
    public SintacticoSemantico(Compilador c) {
        cmp = c;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // Metodo que inicia la ejecucion del analisis sintactico predictivo.
    // analizarSemantica : true = realiza el analisis semantico a la par del sintactico
    //                     false= realiza solo el analisis sintactico sin comprobacion semantica
    public void analizar(boolean analizarSemantica) {
        this.analizarSemantica = analizarSemantica;
        preAnalisis = cmp.be.preAnalisis.complex;

        // * * *   INVOCAR AQUI EL PROCEDURE DEL SIMBOLO INICIAL   * * *
        //P();
        programa();
    }

    //--------------------------------------------------------------------------
    // Procedures hechos por Xavier
    private void _lista_identificadores() {

        if (preAnalisis.equals(",")) {
            emparejar(",");
            emparejar("id");
            _lista_identificadores();
        } else {
            // lista_identificadores' → empty
        }

    }

    private void declaraciones() {

        if (preAnalisis.equals("var")) {
            emparejar("var");
            lista_identificadores();
            emparejar(":");
            tipo();
            emparejar(";");
            declaraciones();
        } else {
            // declaraciones → empty
        }

    }

    private void tipo_estandar() {

        if (preAnalisis.equals("integer")) {
            emparejar("integer");
        } else if (preAnalisis.equals("real")) {
            emparejar("real");
        } else {
            error("[tipo_estandar] Se esperaba 'integer' o 'real'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void declaraciones_subprogramas() {

        if (preAnalisis.equals("function") || preAnalisis.equals("procedure")) {
            _declaraciones_subprogramas();
        } else {
            // declaraciones_subprogramas → empty
        }

    }

    private void _declaraciones_subprogramas() {

        if (preAnalisis.equals("function") || preAnalisis.equals("procedure")) {
            declaracion_subprograma();
            emparejar(";");
            _declaraciones_subprogramas();
        } else {
            // declaraciones_subprogramas' → empty
        }

    }

    private void _lista_proposiciones() {

        if (preAnalisis.equals(";")) {
            emparejar(";");
            proposicion();
            _lista_proposiciones();
        } else {
            // lista_proposiones' → empty
        }

    }

    private void _expresion() {

        if (preAnalisis.equals("oprel")) {
            String izquierdo = tipo_expresion_simple;
            emparejar("oprel");
            expresion_simple();
            String derecho = tipo_expresion_simple;
            
            //Accion 46 / 47 : oprel produce BOOLEANO si operando numéricos
            if (analizarSemantica) {
                if ((ENTERO.equals(izquierdo) || REAL.equals(izquierdo))
                        && (ENTERO.equals(derecho) || REAL.equals(derecho))) {
                    tipo_expresion_prima = BOOLEANO;
                } else {
                    tipo_expresion_prima = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SINTACTICO,
                            "Error de tipos: operador relacional requiere operandos numéricos. Línea: " + cmp.be.preAnalisis.numLinea);
                }
                // Expresion final: si hay expresion' el tipo del whole es BOOLEANO o ERROR_TIPO
                tipo_expresion = tipo_expresion_prima;
            }
        } else {
            // expresion' → empty
        }

    }

    private void proposiciones_optativas() {
        if (preAnalisis.equals("begin")
                || preAnalisis.equals("id")
                || preAnalisis.equals("if")
                || preAnalisis.equals("while")) {

            // proposiciones_optativas → lista_proposiciones
            lista_proposiciones();
        } else {
            // proposiciones_optativas → ε
            // empty
        }
    }

//--------------------------------------------------------------------------
//--------------------------------------------------------------------------
//Procedures hechos por Daniela Lara
    /**
     * programa → program id ( input , output ) ; declaraciones
     * declaraciones_subprogramas proposicion_compuesta .
     */
    private void programa() {
        if (preAnalisis.equals("program")) {
            // program id ( input , output ) ;
            emparejar("program");

            if (preAnalisis.equals("id")) {
                emparejar("id");
            } else {
                error("[programa] Se esperaba identificador después de 'program'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
                return;
            }

            emparejar("(");

            if (preAnalisis.equals("input")) {
                emparejar("input");
            } else {
                error("[programa] Se esperaba 'input'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
                return;
            }

            emparejar(",");

            if (preAnalisis.equals("output")) {
                emparejar("output");
            } else {
                error("[programa] Se esperaba 'output'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
                return;
            }

            emparejar(")");
            emparejar(";");

            // declaraciones
            declaraciones();

            // declaraciones_subprogramas
            declaraciones_subprogramas();

            // proposicion_compuesta
            proposicion_compuesta();

            // . (punto final del programa)
            if (preAnalisis.equals(".")) {
                emparejar(".");
            } else {
                error("[programa] Se esperaba '.' al final del programa.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
            }

        } else {
            error("[programa] Se esperaba 'program' al inicio del programa.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void lista_identificadores() {
        if (preAnalisis.equals("id")) {
            emparejar("id");
            _lista_identificadores();
        } else {
            error("[lista_identificadores] Se esperaba identificador.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void tipo() {
        if (preAnalisis.equals("integer") || preAnalisis.equals("real")) {
            tipo_estandar();
        } else if (preAnalisis.equals("array")) {
            emparejar("array");
            emparejar("[");
            if (preAnalisis.equals("num")) {
                emparejar("num");
            } else {
                error("[tipo] Se esperaba 'num'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
            }
            emparejar(".");
            emparejar(".");
            if (preAnalisis.equals("num")) {
                emparejar("num");
            } else {
                error("[tipo] Se esperaba 'num'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
            }
            emparejar("]");
            emparejar("of");
            tipo_estandar();
        } else {
            error("[tipo] Se esperaba 'integer', 'real' o 'array'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void declaracion_subprograma() {
        encab_subprograma();
        declaraciones();
        proposicion_compuesta();
    }

    private void encab_subprograma() {
        if (preAnalisis.equals("function")) {
            emparejar("function");
            if (preAnalisis.equals("id")) {
                emparejar("id");
            } else {
                error("[encab_subprograma] Se esperaba identificador.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
            }
            argumentos();
            emparejar(":");
            tipo_estandar();
            emparejar(";");
        } else if (preAnalisis.equals("procedure")) {
            emparejar("procedure");
            if (preAnalisis.equals("id")) {
                emparejar("id");
            } else {
                error("[encab_subprograma] Se esperaba identificador.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
            }
            argumentos();
            emparejar(";");
        } else {
            error("[encab_subprograma] Se esperaba 'function' o 'procedure'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void argumentos() {
        if (preAnalisis.equals("(")) {
            emparejar("(");
            lista_parametros();
            emparejar(")");
        } else {
            // argumentos → ε
        }
    }

    private void lista_parametros() {
        if (preAnalisis.equals("id")) {
            lista_identificadores();
            emparejar(":");
            tipo();
            _lista_parametros();
        } else {
            error("[lista_parametros] Se esperaba identificador.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void _lista_parametros() {
        if (preAnalisis.equals(";")) {
            emparejar(";");
            lista_identificadores();
            emparejar(":");
            tipo();
            _lista_parametros();
        } else {
            // lista_parametros' → ε
        }
    }

//Procedure hechos por Yessenia Verónica Morones Dovalí
    private void proposicion_compuesta() {
        //proposicion_compuesta → begin proposiciones_optativas end
        if (preAnalisis.equals("begin")) {
            //begin proposiciones_optativas end
            emparejar("begin");
            proposiciones_optativas();
            emparejar("end");
        } else {
            error("[proposicion_compuesta] Se esperaba 'begin'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void lista_proposiciones() {
        // lista_proposiciones → proposicion lista_proposiciones’
        proposicion();
        _lista_proposiciones();
    }

    private void proposicion() {
        // proposicion → id proposicion’ 
        //             | poposicion_compuesta
        //             | if expresion then proposicion else proposicion
        //             | while expresion do proposicion
        if (preAnalisis.equals("id")) {
            // proposicion → id proposicion’ 
            emparejar("id");
            _proposicion();
        } else if (preAnalisis.equals("begin")) {
            // proposicion → proposicion_compuesta
            proposicion_compuesta();
        } else if (preAnalisis.equals("if")) {
            // proposicion → if expresion then proposicion else proposicion
            emparejar("if");
            expresion();
            emparejar("then");
            proposicion();
            emparejar("else");
            proposicion();
        } else if (preAnalisis.equals("while")) {
            // proposicion → while expresion do proposicion
            emparejar("while");
            expresion();
            emparejar("do");
            proposicion();
        } else {
            error("[proposicion] Se esperaba 'id', 'begin', 'if' o 'while'.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void _proposicion() {
        // proposicion’ → variable opasig expresion
        //              | opasig Expresion
        //              | Proposicion_Procedimiento
        //              | ε
        if (preAnalisis.equals("[")) {
            //proposicion’ → variable opasig expresion
            variable();
            emparejar("opasig");
            expresion();
        } else if (preAnalisis.equals("opasig")) {
            //proposicion’ → opasig expresion
            emparejar("opasig");
            expresion();
        } else if (preAnalisis.equals("(")) {
            //proposicion’ → proposicion_procedimiento
            proposicion_procedimiento();
        } else {
            //proposicion’ → ε
        }
    }

    private void proposicion_procedimiento() {
        //proposicion_procedimiento → proposicion_procedimiento’
        _proposicion_procedimiento();
    }

    private void _proposicion_procedimiento() {
        //proposicion_procedimiento’ → ( lista_expresiones )  | ϵ
        if (preAnalisis.equals("(")) {
            emparejar("(");
            lista_expresiones();
            emparejar(")");
        } else {
            //proposicion_procedimiento’ → ϵ
        }
    }

    private void lista_expresiones() {
        // lista_expresiones → expresion lista_expresiones´
        if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("(")) {
            // expresion lista_expresiones´
            expresion();
            _lista_expresiones();
        } else {
            error("[lista_expresiones] Se esperaba expresión.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void _lista_expresiones() {
        // lista_expresiones’ → , lista_expresiones | ε
        if (preAnalisis.equals(",")) {
            // , lista_expresiones
            emparejar(",");
            lista_expresiones();
        } else {
            // ε
        }
    }

//--------------------------------------------------------------------------
//--------------------------------------------------------------------------
//Procedures hechos por Daniela Aldaco
    //Acciones semanticas hechas por Yessenia Morones
    private void expresion() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num")
                || preAnalisis.equals("(") || preAnalisis.equals("num.num")) {
            expresion_simple();
            _expresion();
            
            //accion 45
            if(analizarSemantica){
                if(tipo_expresion == null){
                    tipo_expresion = tipo_expresion_simple;
                }
            }
        } else {
            error("[expresion] Se esperaba inicio de expresión.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void expresion_simple() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num")
                || preAnalisis.equals("(") || preAnalisis.equals("num.num")) {
            // termino() dejará su tipo en tipo_termino
            termino();
            // Inicialmente expresion_simple toma el tipo del termino
            tipo_expresion_simple = tipo_termino;
            // procesar posibles +/-
            _expresion_simple();
            // Si _expresion_simple dejó un tipo distinto, ese es el resultado
            // Accion 49 / 50
            if (analizarSemantica) {
                if (tipo_expresion_simple == null) {
                    tipo_expresion_simple = tipo_termino; 
                }
            }
        } else {
            error("[expresion_simple] Se esperaba inicio de expresión simple.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

     private void _expresion_simple() {
        if (preAnalisis.equals("opsuma")) {
            // operador suma presente: opsuma termino expresion_simple’
            // guardamos tipo izquierdo actual
            String izquierdo = tipo_expresion_simple;

            emparejar("opsuma");
            termino();
            String derecho = tipo_termino;

            // combinar tipos entre izquierdo y derecho (regla 49)
            if (analizarSemantica) {
                String combinado = combinaTiposAritmeticos(izquierdo, derecho);
                if (ERROR_TIPO.equals(combinado)) {
                    tipo_expresion_simple_prima = ERROR_TIPO;
                    tipo_expresion_simple = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SINTACTICO,
                            "Error de tipos en suma/ resta. Línea: " + cmp.be.preAnalisis.numLinea);
                } else {
                    tipo_expresion_simple_prima = combinado;
                    tipo_expresion_simple = combinado;
                }
            }

            // recursión para más opsuma
            _expresion_simple();

        } else if (preAnalisis.equals("oprel") || preAnalisis.equals(")")
                || preAnalisis.equals("]") || preAnalisis.equals("then")
                || preAnalisis.equals("do") || preAnalisis.equals(";")
                || preAnalisis.equals("end") || preAnalisis.equals("else")
                || preAnalisis.equals(",")) {
            // ε-producción -> expresion_simple' := VACIO (regla 50)
            if (analizarSemantica) {
                if (tipo_expresion_simple_prima == null) tipo_expresion_simple_prima = VACIO;
            }
        } else {
            error("[expresion_simple’] Se esperaba 'opsuma' o fin de expresión simple.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void termino() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num")
                || preAnalisis.equals("(") || preAnalisis.equals("num.num")) {
            factor();
            // tipo_termino inicialmente es el tipo del factor
            tipo_termino = tipo_factor;
            _termino();
            // si no hay parte prima, tipo_termino ya quedó en tipo_factor
        } else {
            error("[termino] Se esperaba inicio de término.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void _termino() {
        if (preAnalisis.equals("opmult")) {
            // opmult factor termino'
            String izquierdo = tipo_termino;

            emparejar("opmult");
            factor();
            String derecho = tipo_factor;

            // combinar tipos multiplicativos (regla 51/52)
            if (analizarSemantica) {
                String combinado = combinaTiposAritmeticos(izquierdo, derecho);
                if (ERROR_TIPO.equals(combinado)) {
                    tipo_termino_prima = ERROR_TIPO;
                    tipo_termino = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SINTACTICO,
                            "Error de tipos en operador multiplicativo. Línea: " + cmp.be.preAnalisis.numLinea);
                } else {
                    tipo_termino_prima = combinado;
                    tipo_termino = combinado;
                }
            }

            _termino(); // recursión
        } else if (preAnalisis.equals("opsuma") || preAnalisis.equals("oprel")
                || preAnalisis.equals(")") || preAnalisis.equals("]")
                || preAnalisis.equals("then") || preAnalisis.equals("do")
                || preAnalisis.equals(";") || preAnalisis.equals("end")
                || preAnalisis.equals("else") || preAnalisis.equals(",")) {
            // ε-producción -> termino' := VACIO (regla 53)
            if (analizarSemantica) {
                if (tipo_termino_prima == null) tipo_termino_prima = VACIO;
            }
        } else {
            error("[termino’] Se esperaba 'opmult' o fin de término.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void factor() {
        if (preAnalisis.equals("id")) {
            // guardamos lexema del id antes de avanzar
            String idLex = cmp.be.preAnalisis.lexema;
            emparejar("id");

            // regla 54: buscar tipo en TS
            if (analizarSemantica) {
                int pos = cmp.ts.buscar(idLex);
                if (pos > 0) {
                    tipo_factor = cmp.ts.buscaTipo(pos); // asume que buscaTipo devuelve String del tipo
                } else {
                    tipo_factor = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SINTACTICO,
                            "Identificador no declarado: " + idLex + ". Línea: " + cmp.be.preAnalisis.numLinea);
                }
            }

            // factor' (posible llamada)
            _factor();

            // Si factor' representó una llamada a función, la semántica de _factor puede actualizar tipo_factor
            // (mantener lo que ya se haya fijado)
        } else if (preAnalisis.equals("num")) {
            emparejar("num");
            // regla 55
            if (analizarSemantica) {
                tipo_factor = ENTERO;
            }
        } else if (preAnalisis.equals("num.num")) {
            emparejar("num.num");  // num.num
            // regla 56
            if (analizarSemantica) {
                tipo_factor = REAL;
            }
        } else if (preAnalisis.equals("(")) {
            emparejar("(");
            // ( expresion )
            expresion();
            emparejar(")");
            // regla 57: factor.tipo := expresion.tipo
            if (analizarSemantica) {
                tipo_factor = tipo_expresion;
            }
        } else {
            error("[factor] Se esperaba 'id', 'num' o '(' en factor.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void _factor() {
        if (preAnalisis.equals("(")) {
            // llamada a procedimiento/función o lista de exp
            emparejar("(");
            lista_expresiones();
            emparejar(")");

            // regla 58: factor'.tipo := verificaParametros(lista_expresiones)
            if (analizarSemantica) {
                // Por simplicidad llamamos a un verificador que devuelve 'void' o tipo de retorno
                // en una implementacion completa verificarías firmas en la tabla de simbolos
                tipo_factor_prima = verificaParametros();
                // Si la llamada representa invocación de función y devuelve un tipo,
                // deberías asignarlo a tipo_factor (en esta plantilla dejamos tipo_factor como está
                // salvo que quieras mapearlo a tipo_factor_prima)
                if (!VOID.equals(tipo_factor_prima) && tipo_factor_prima != null) {
                    tipo_factor = tipo_factor_prima;
                }
            }
        } else if (preAnalisis.equals("opmult") || preAnalisis.equals("opsuma")
                || preAnalisis.equals("oprel") || preAnalisis.equals(")")
                || preAnalisis.equals("]") || preAnalisis.equals("then")
                || preAnalisis.equals("do") || preAnalisis.equals(";")
                || preAnalisis.equals("end") || preAnalisis.equals("else")
                || preAnalisis.equals(",")) {
            // ε-producción -> regla 59
            if (analizarSemantica) {
                tipo_factor_prima = VOID;
            }
        } else {
            error("[factor’] Se esperaba '(' o fin de factor.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void variable() {
        if (preAnalisis.equals("[")) {
            emparejar("[");
            expresion();
            emparejar("]");
        } else {
            error("[variable] Se esperaba '[' en variable.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }
    
    // -------------------- Auxiliares semánticos --------------------

    /**
     * Combina dos tipos aritméticos aplicando las reglas:
     * ENTERO + ENTERO -> ENTERO
     * ENTERO + REAL -> REAL
     * REAL + ENTERO -> REAL
     * REAL + REAL -> REAL
     * cualquier otro caso -> ERROR_TIPO
     */
    private String combinaTiposAritmeticos(String a, String b) {
        if (a == null || b == null) return ERROR_TIPO;
        if (ERROR_TIPO.equals(a) || ERROR_TIPO.equals(b)) return ERROR_TIPO;

        if (ENTERO.equals(a) && ENTERO.equals(b)) return ENTERO;
        if (ENTERO.equals(a) && REAL.equals(b)) return REAL;
        if (REAL.equals(a) && ENTERO.equals(b)) return REAL;
        if (REAL.equals(a) && REAL.equals(b)) return REAL;
        // si alguno no es numérico -> error
        return ERROR_TIPO;
    }

    /**
     * Verificador simple de parámetros de llamada.
     * En una implementación completa se consultaría la firma de la función/procedimiento
     * en la tabla de símbolos, comparando cantidad y tipos.
     * Aquí devolvemos "void" como placeholder para llamadas a procedimiento,
     * o podrías devolver el tipo de retorno si tu TS tiene esa info.
     */
    private String verificaParametros() {
        // Implementación mínima: asumimos que la verificación pasa y la llamada es a procedimiento -> void
        // Cambia esto cuando dispongas de las firmas de rutinas en cmp.ts
        return VOID;
    }

    //--------------------------------------------------------------------------
    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;
        } else {
            errorEmparejar(t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
    private void errorEmparejar(String _token, String _lexema, int numLinea) {
        String msjError = "";

        if (_token.equals("id")) {
            msjError += "Se esperaba un identificador";
        } else if (_token.equals("num")) {
            msjError += "Se esperaba una constante entera";
        } else if (_token.equals("num.num")) {
            msjError += "Se esperaba una constante real";
        } else if (_token.equals("literal")) {
            msjError += "Se esperaba una literal";
        } else if (_token.equals("oparit")) {
            msjError += "Se esperaba un operador aritmetico";
        } else if (_token.equals("oprel")) {
            msjError += "Se esperaba un operador relacional";
        } else if (_token.equals("opasig")) {
            msjError += "Se esperaba operador de asignacion";
        } else {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + (_lexema.equals("$") ? "fin de archivo" : _lexema)
                + ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico
    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }

    // Fin de error
    //--------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUI EL CODIGO DE LOS PROCEDURES  *  *  *  *
    //--------------------------------------------------------------------------
    private void P() {
        if (preAnalisis.equals("id") || preAnalisis.equals("inicio")) {
            V();
            C();
        } else {
            error("(P) → Programa debe empezar con un id o palabra inicio\n"
                    + "No. linea:" + cmp.be.preAnalisis.numLinea);
        }
    }

    private void V() {
        if (preAnalisis.equals("id")) {
            emparejar("id");
            emparejar(":");
            T();
            V();
        } else {
            // V -> empty
        }

    }

    private void T() {
        if (preAnalisis.equals("entero")) {
            emparejar("entero");
        } else if (preAnalisis.equals("real")) {
            emparejar("real");
        } else if (preAnalisis.equals("caracter")) {
            emparejar("caracter");
        } else {
            error("Se esperaba un tipo de dato");
        }
    }

    private void C() {
        if (preAnalisis.equals("inicio")) {
            emparejar("inicio");
            S();
            emparejar("fin");
        } else {
            error("(C) → se esperaba la palabra 'inicio'\n"
                    + "No. linea:" + cmp.be.preAnalisis.numLinea);
        }
    }

    private void S() {
        if (preAnalisis.equals("id")) {
            emparejar("id");
            emparejar("opasig");
            E();
            S();
        } else {
            // S -> empty
        }
    }

    private void E() {
        if (preAnalisis.equals("id")) {
            // E -> id
            emparejar("id");
        } else if (preAnalisis.equals("num")) {
            emparejar("num");
            if (preAnalisis.equals(".")) {
                emparejar(".");
                emparejar("num");
            }
        } else {
            error("(E) → E no produce empty");
        }
    }

    /*----------------------------------------------------------------------------------------*/
    // AUTOR: Ing. Fernando Gil
    // Metodo para comprobar la existencia en disco del archivo "nomarchivo" 
    // y en su caso cargar su contenido en la Tabla de Simbolos.
    // El argumento representa el nombre de un archivo de texto con extension
    //  ".db" que contiene el esquema (diseño) de una tabla de base de datos. 
    // Los archivos .db tienen el siguiente diseño:
    //     Dato            ColIni    ColFin
    //     ==================================
    //     nombre-columna  1         25
    //     tipo-de-dato    30        40
    //
    // Ejemplo:  alumnos.db
    //          1         2         3        
    // 1234567890123456789012345678901234567890
    // ==========================================
    // numctrl                      char(8)  
    // nombre                       char(25)
    // edad                         int
    // promedio                     float
    //
    // Cada columna se carga en la Tabla de Simbolos con Complex = "id" y
    // Tipo = "columna(t)"  siendo t  el tipo de dato de la columna.
    // ----------------------------------------------------------------------
    // 20/Oct/2018: Si en la T.S. ya existe la columna con el mismo ambito 
    // que el que se va a registrar solo se sustituye el TIPO si está en blanco.
    // Si existe la columna pero no tiene ambito entonces se rellenan los datos
    // del tipo y el ambito. 
    private boolean checarArchivo(String nomarchivo) {
        FileReader fr = null;
        BufferedReader br = null;
        String linea = null;
        String columna = null;
        String tipo = null;
        String ambito = null;
        boolean existeArch = false;
        int pos;

        try {
            // Intentar abrir el archivo con el diseño de la tabla  
            fr = new FileReader(nomarchivo);
            cmp.ts.anadeTipo(cmp.be.preAnalisis.getEntrada(), "tabla");
            br = new BufferedReader(fr);

            // Leer linea x linea, cada linea es la especificacion de una columna
            linea = br.readLine();
            while (linea != null) {
                // Extraer nombre y tipo de dato de la columna
                try {
                    columna = linea.substring(0, 24).trim();
                } catch (Exception err) {
                    columna = "ERROR";
                }
                try {
                    tipo = linea.substring(29).trim();
                } catch (Exception err) {
                    tipo = "ERROR";
                }
                try {
                    ambito = nomarchivo.substring(0, nomarchivo.length() - 3);
                } catch (Exception err) {
                    ambito = "ERROR";
                }
                // Agregar a la tabla de simbolos
                Linea_TS lts = new Linea_TS("id",
                        columna,
                        "COLUMNA(" + tipo + ")",
                        ambito
                );
                // Checar si en la Tabla de Simbolos existe la entrada para un 
                // lexema y ambito iguales al de columna y ambito de la tabla .db
                if ((pos = cmp.ts.buscar(columna, ambito)) > 0) {
                    // YA EXISTE: Si no tiene tipo asignarle el tipo columna(t) 
                    if (cmp.ts.buscaTipo(pos).trim().isEmpty()) {
                        cmp.ts.anadeTipo(pos, tipo);
                    }
                } else {
                    // NO EXISTE: Buscar si en la T. de S. existe solo el lexema de la columna
                    if ((pos = cmp.ts.buscar(columna)) > 0) {
                        // SI EXISTE: checar si el ambito esta en blanco
                        Linea_TS aux = cmp.ts.obt_elemento(pos);
                        if (aux.getAmbito().trim().isEmpty()) {
                            // Ambito en blanco rellenar el tipo y el ambito  
                            cmp.ts.anadeTipo(pos, "COLUMNA(" + tipo + ")");
                            cmp.ts.anadeAmbito(pos, ambito);

                        } else {
                            // Insertar un nuevo elemento a la tabla de simb.
                            cmp.ts.insertar(lts);
                        }
                    } else {
                        // NO EXISTE: insertar un nuevo elemento a la tabla de simb.
                        cmp.ts.insertar(lts);
                    }
                }

                // Leer siguiente linea
                linea = br.readLine();
            }
            existeArch = true;
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            // Cierra los streams de texto si es que se crearon
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex) {
            }
        }
        return existeArch;
    }

    /*----------------------------------------------------------------------------------------*/
}
//------------------------------------------------------------------------------
//::
