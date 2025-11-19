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

import general.Linea_BE;
import general.Linea_TS;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;


public class SintacticoSemantico {
    
    public static final String VACIO      = "vacio";
    public static final String ERROR_TIPO = "error_tipo";
    
    private java.util.List<String> lista_entradas_id = new java.util.ArrayList<>();


    private Compilador cmp;
    private boolean analizarSemantica = false;
    private String preAnalisis;


    
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

        Atributos atrPrograma = new Atributos();
        P(atrPrograma);

        if (analizarSemantica) {
            if (atrPrograma.tipo.equals(VACIO)) {
                System.out.println("Programa.tipo = VACIO");
            } else {
                System.out.println("Programa.tipo = ERROR_TIPO");
            }
        }
}


    //--------------------------------------------------------------------------
    // Procedures hechos por Xavier
    private void _lista_identificadores(Atributos listaPrima) {

    if (preAnalisis.equals(",")) {

        emparejar(",");
        Linea_BE idActual = cmp.be.preAnalisis;
        emparejar("id");

        // ===== AS7 ==========================================
        if (analizarSemantica) {
            lista_entradas_id.add(idActual.lexema); // agrega el id
        }

        // Llamar recursivamente
        Atributos listaPrima2 = new Atributos();
        _lista_identificadores(listaPrima2);

        // ===== AS61 ==========================================
        if (analizarSemantica) {
            listaPrima.tipo = listaPrima2.tipo;
        }

    } 
    
    else {
        // ===== AS62 ==========================================
        if (analizarSemantica) {
            listaPrima.tipo = VACIO;
        }
    }
}


       private void declaraciones(Atributos declaraciones) {

    // ----------- CASO ε (AS3) -----------------
    if (!preAnalisis.equals("var")) {
        if (analizarSemantica) {
            declaraciones.tipo = VACIO;   // AS3
        }
        return;
    }

    // ----------- CASO  var lista_identificadores : tipo ; declaraciones -----------
    emparejar("var");

    // atributos auxiliares
    Atributos lista = new Atributos();
    Atributos tipo  = new Atributos();
    Atributos declaraciones1 = new Atributos();

    // ===== lista_identificadores =====
    lista_identificadores(lista);

    emparejar(":");

    // ===== tipo =====
    tipo(tipo);

    // =================== AS4 ===================
    if (analizarSemantica) {

        declaraciones.tipoAux = VACIO; // bandera

        for (String entrada : lista_entradas_id) {

            int pos = cmp.ts.buscar(entrada);  
            String tipoExistente = (pos > 0) ? cmp.ts.buscaTipo(pos) : "";

            if (tipoExistente == null || tipoExistente.isEmpty()) {

                // ----------- NO EXISTE → insertar primero -----------
                Linea_TS nueva = new Linea_TS(
                    "id",
                    entrada,
                    "",      // tipo vacío
                    ""       // ambito vacío
                );

                pos = cmp.ts.insertar(nueva); // ahora sí existe

                // ----------- ASIGNAR TIPO CORRECTAMENTE -----------
                cmp.ts.anadeTipo(pos, tipo.tipo);

            } else {

                // ya estaba declarado → ERROR
                declaraciones.tipoAux = ERROR_TIPO;

                cmp.me.error(
                    Compilador.ERR_SEMANTICO,
                    "[declaraciones] Identificador ya declarado: " + entrada
                );
            }
        }

        lista_entradas_id.clear();  // limpiar la lista
    }

    emparejar(";");

    // ===== declaraciones1 =====
    declaraciones(declaraciones1);

    // =================== AS5 ===================
    if (analizarSemantica) {
        if (declaraciones.tipoAux.equals(VACIO)
                && declaraciones1.tipo.equals(VACIO)) {

            declaraciones.tipo = VACIO;

        } else {
            declaraciones.tipo = ERROR_TIPO;
        }
    }
}


        private void tipo_estandar(Atributos te) {

        if (preAnalisis.equals("integer")) {
            emparejar("integer");
            if (analizarSemantica) te.tipo = "integer";   // AS10
        }
        else if (preAnalisis.equals("real")) {
            emparejar("real");
            if (analizarSemantica) te.tipo = "real";      // AS11
        }
        else {
            error("[tipo_estandar] Se esperaba integer o real.");
        }
    }

    

        private void declaraciones_subprogramas(Atributos dsp) {

        // ======== caso ε ========  (AS13)
        if (!preAnalisis.equals("function") && !preAnalisis.equals("procedure")) {
            if (analizarSemantica) {
                dsp.tipo = VACIO;   // AS13
            }
            return;
        }
        Atributos dspPrima = new Atributos();
        _declaraciones_subprogramas(dspPrima);

        // AS12
        if (analizarSemantica) {
            dsp.tipo = dspPrima.tipo;
        }
    }


    private void _lista_proposiciones(Atributos listaPrima) {

    // ===== caso ; proposicion lista_proposiciones' =====
    if (preAnalisis.equals(";")) {

        emparejar(";");

        Atributos prop = new Atributos();
        Atributos listaPrima2 = new Atributos();

        proposicion(prop);
        _lista_proposiciones(listaPrima2);

        // NO TIENE AS explícito pero para que funcione con AS27:
        if (analizarSemantica) {

            if (prop.tipo.equals(VACIO) && listaPrima2.tipo.equals(VACIO)) {
                listaPrima.tipo = VACIO;
            } else {
                listaPrima.tipo = ERROR_TIPO;
            }
        }

        return;
    }

    // ===== caso ε =====
    if (analizarSemantica)
        listaPrima.tipo = VACIO;  // porque ε no produce nada
}


    private void _expresion() {

        if (preAnalisis.equals("oprel")) {
            emparejar("oprel");
            expresion_simple();
        } else {
            // expresion' → empty
        }

    }

        private void proposiciones_optativas(Atributos propOpt) {

        // ======== CASO lista_proposiciones  =========
        if (preAnalisis.equals("id")
            || preAnalisis.equals("begin")
            || preAnalisis.equals("if")
            || preAnalisis.equals("while")) {

            Atributos lista = new Atributos();

            lista_proposiciones(lista);

            // ---------- AS25 ----------
            if (analizarSemantica) {
                propOpt.tipo = lista.tipo;
            }

            return;
        }

        // ========= CASO ε ============
        // AS26
        if (analizarSemantica) {
            propOpt.tipo = "void";    // O así te lo pidieron textual
        }
    }


//--------------------------------------------------------------------------
//--------------------------------------------------------------------------
//Procedures hechos por Daniela Lara
    /**
     * programa → program id ( input , output ) ; declaraciones
     * declaraciones_subprogramas proposicion_compuesta .
     */
   private void programa(Atributos Programa) {

    Atributos declaracionesAtr = new Atributos();
    Atributos declaracionesSubAtr = new Atributos();
    Atributos propCompuestaAtr = new Atributos();

    String nombrePrograma = "";

    if (preAnalisis.equals("program")) {

        emparejar("program");

        // === Guardar ID del programa ===
        if (preAnalisis.equals("id")) {
            nombrePrograma = cmp.be.preAnalisis.lexema;
            emparejar("id");
        } else {
            error("[programa] Se esperaba identificador después de 'program'.\nLínea: "
                    + cmp.be.preAnalisis.numLinea);
            return;
        }

        emparejar("(");
        emparejar("input");
        emparejar(",");
        emparejar("output");
        emparejar(")");
        emparejar(";");

        declaraciones(declaracionesAtr);
        declaraciones_subprogramas(declaracionesSubAtr);
        proposicion_compuesta(propCompuestaAtr);

        emparejar(".");

    } else {
        error("[programa] Se esperaba 'program'. Línea: " + cmp.be.preAnalisis.numLinea);
        return;
    }

    // ============================ AS1 ============================
    if (analizarSemantica) {

        int pos = cmp.ts.buscar(nombrePrograma);

        if (pos > 0) {
            // Ya existe → solo asignamos tipo si no tiene
            cmp.ts.anadeTipo(pos, "program");
        } else {
            // No existe → se inserta y luego se asigna tipo
            Linea_TS nueva = new Linea_TS(
                    "id",
                    nombrePrograma,
                    "",
                    ""                  // ambito vacío
            );
            pos = cmp.ts.insertar(nueva);
            cmp.ts.anadeTipo(pos, "program");
        }
    }
    // =============================================================

    // ============================ AS2 ============================
    if (analizarSemantica) {
        if (declaracionesAtr.tipo.equals(VACIO) &&
            declaracionesSubAtr.tipo.equals(VACIO) &&
            propCompuestaAtr.tipo.equals(VACIO)) {

            Programa.tipo = VACIO;

        } else {
            Programa.tipo = ERROR_TIPO;
            cmp.me.error(Compilador.ERR_SEMANTICO,
                    "[programa] El programa contiene errores de tipo.");
        }
    }
    // =============================================================
}


    private void lista_identificadores(Atributos lista) {

    Atributos listaPrima = new Atributos();

    Linea_BE idActual = cmp.be.preAnalisis;

    if (preAnalisis.equals("id")) {

        emparejar("id");

        // ===== AS6 ==========================================
        if (analizarSemantica) {
            lista_entradas_id.add(idActual.lexema);  
        }

        // Llamar a la parte prima
        _lista_identificadores(listaPrima);

        // ===== AS60 ==========================================
        if (analizarSemantica) {
            lista.tipo = listaPrima.tipo;
        }

    } else {
        error("[lista_identificadores] Se esperaba un identificador.");
    }
}


    private void tipo(Atributos tipo) {
    if (preAnalisis.equals("integer") || preAnalisis.equals("real")) {

        Atributos te = new Atributos();
        tipo_estandar(te);

        if (analizarSemantica)
            tipo.tipo = te.tipo;   // AS8

        return;
    }

    if (preAnalisis.equals("array")) {

        emparejar("array");
        emparejar("[");

        // -------- num_inf --------
        Linea_BE tokenNumInf = cmp.be.preAnalisis;

        if (!preAnalisis.equals("num")) {
            error("[tipo] Se esperaba número inferior en rango.");
            return;
        }

        int numInf = Integer.parseInt(tokenNumInf.lexema);
        emparejar("num"); // consume num_inf

        emparejar(".");
        emparejar(".");

        // -------- num_sup --------
        Linea_BE tokenNumSup = cmp.be.preAnalisis;

        if (!preAnalisis.equals("num")) {
            error("[tipo] Se esperaba número superior en rango.");
            return;
        }

        int numSup = Integer.parseInt(tokenNumSup.lexema);
        emparejar("num"); // consume num_sup

        emparejar("]");
        emparejar("of");

        // -------- tipo_estandar --------
        Atributos te = new Atributos();
        tipo_estandar(te);

        // ======== AS9 ========
        if (analizarSemantica) {

            boolean ok = true;

            if (numInf > numSup) ok = false;

            if (ok) {
                tipo.tipo = "ARRAY(" + numInf + ".." + numSup + "," + te.tipo + ")";
            } else {
                tipo.tipo = ERROR_TIPO;
                cmp.me.error(Compilador.ERR_SEMANTICO,
                    "[tipo] Rango inválido en arreglo: " + numInf + " .. " + numSup);
            }
        }

        return;
    }

    // ----------- Error si no es ninguno -----------
    error("[tipo] Se esperaba integer, real o array.");
}


    private void declaracion_subprograma(Atributos dsub) {

        Atributos encab = new Atributos();
        Atributos decls = new Atributos();
        Atributos prop  = new Atributos();

        // ===== encab_subprograma =====
        encab_subprograma(encab);

        // ===== declaraciones =====
        declaraciones(decls);

        // ===== proposicion_compuesta =====
        proposicion_compuesta(prop);

        // ===== AS16 =====
        if (analizarSemantica) {
            if ( encab.tipo.equals(VACIO)
                 && decls.tipo.equals(VACIO)
                 && prop.tipo.equals(VACIO) ) {

                dsub.tipo = VACIO;

            } else {
                dsub.tipo = ERROR_TIPO;
            }
        }
    }


    private void encab_subprograma(Atributos encab) {

    Linea_BE idActual = new Linea_BE();
    Atributos tipoStd = new Atributos();

    // ---------- CASO function ----------
    if (preAnalisis.equals("function")) {

        emparejar("function");

        idActual = cmp.be.preAnalisis;   // Guardar el ID
        emparejar("id");

        // argumentos
        argumentos(new Atributos());

        emparejar(":");

        // tipo_estandar
        tipo_estandar(tipoStd);

        emparejar(";");

        // ======== AS17 =========
        if (analizarSemantica) {

            String tipoEncontrado = cmp.ts.buscaTipo(idActual.entrada);

            if (tipoEncontrado == null || tipoEncontrado.equals("")) {

                // Registrar la función con su tipo de retorno
                cmp.ts.anadeTipo(idActual.entrada, tipoStd.tipo);

                encab.tipo = VACIO;

            } else {

                encab.tipo = ERROR_TIPO;

                cmp.me.error(
                    Compilador.ERR_SEMANTICO,
                    "[encab_subprograma] Identificador duplicado en function: "
                    + idActual.lexema
                );
            }
        }

        return;
    }

    // ---------- CASO procedure ----------
    else if (preAnalisis.equals("procedure")) {

        emparejar("procedure");

        idActual = cmp.be.preAnalisis;
        emparejar("id");

        // argumentos
        argumentos(new Atributos());

        // ======== AS18 =========
        if (analizarSemantica) {

            String tipoEncontrado = cmp.ts.buscaTipo(idActual.entrada);

            if (tipoEncontrado == null || tipoEncontrado.equals("")) {

                // Registrar el procedimiento
                cmp.ts.anadeTipo(idActual.entrada, "procedure");

                encab.tipo = VACIO;

            } else {

                encab.tipo = ERROR_TIPO;

                cmp.me.error(
                    Compilador.ERR_SEMANTICO,
                    "[encab_subprograma] Identificador duplicado en procedure: "
                    + idActual.lexema
                );
            }
        }

        return;
    }

    // ----------- ERROR si no es ninguno ------------
    error("[encab_subprograma] Se esperaba 'function' o 'procedure'.");
}



        private void argumentos(Atributos argumentos) {

        // ----------- CASO ( lista_parametros )  -----------   {19}
        if (preAnalisis.equals("(")) {

            emparejar("(");

            Atributos lista = new Atributos();
            lista_parametros(lista);

            emparejar(")");

            // ===== AS19 =====
            if (analizarSemantica) {
                argumentos.tipo = lista.tipo;   // tipo devuelto por la lista
            }

            return;
        }

        // ----------- CASO ε  ------------------------------   {20}
        if (analizarSemantica) {
            argumentos.tipo = "void";     // AS20
        }
    }


        private void lista_parametros(Atributos lp) {

        // ====== atributos auxiliares ======
        Atributos listaIds = new Atributos();
        Atributos tipo     = new Atributos();
        Atributos lpPrima  = new Atributos();

        // ----- lista_parametros → lista_identificadores : tipo lista_parametros' -----

        if (preAnalisis.equals("id")) {

            // --- lista_identificadores ---
            lista_identificadores(listaIds);

            emparejar(":");

            // --- tipo ---
            tipo(tipo);

            // === AS21 ===
            if (analizarSemantica) {
                lp.tipo = tipo.tipo;   // el tipo de TODOS los parámetros
            }

            // --- lista_parametros' ---
            _lista_parametros(lpPrima);

            return;
        }

        error("[lista_parametros] Se esperaba un identificador para parámetros.\nLínea: "
              + cmp.be.preAnalisis.numLinea);
    }


        private void _lista_parametros(Atributos lpPrima) {

        // --------- CASO ; lista_identificadores : tipo lista_parametros' -----------
        if (preAnalisis.equals(";")) {

            emparejar(";");

            Atributos listaIds = new Atributos();
            lista_identificadores(listaIds);

            emparejar(":");

            Atributos tipo = new Atributos();
            tipo(tipo);

            Atributos lpPrima2 = new Atributos();
            _lista_parametros(lpPrima2);

            // ==================== AS22 =====================
            if (analizarSemantica) {

                if (!tipo.tipo.equals(ERROR_TIPO) &&
                     lpPrima2.tipo.equals(VACIO)) {

                    lpPrima.tipo = VACIO;

                } else {
                    lpPrima.tipo = ERROR_TIPO;
                }
            }

            return;
        }

        // --------- CASO ε  (AS23) ----------
        if (analizarSemantica) {
            lpPrima.tipo = "void";   // AS23
        }
    }


private void proposicion_compuesta(Atributos propCompuesta) {

    Atributos propOptativas = new Atributos();

    if (preAnalisis.equals("begin")) {

        emparejar("begin");

        // ===== proposiciones_optativas =====
        proposiciones_optativas(propOptativas);

        emparejar("end");

        // ========== AS24 ==========
        if (analizarSemantica) {
            if (propOptativas.tipo.equals(VACIO)) {
                propCompuesta.tipo = VACIO;
            } else {
                propCompuesta.tipo = ERROR_TIPO;
            }
        }

    } else {
        error("[proposicion_compuesta] Se esperaba 'begin'. Línea: "
              + cmp.be.preAnalisis.numLinea);
    }
}

        private void lista_proposiciones(Atributos lista) {

        Atributos prop = new Atributos();
        Atributos listaPrima = new Atributos();

        // ====== proposicion ======
        proposicion(prop);

        // ====== lista_proposiciones' ======
        _lista_proposiciones(listaPrima);

        // ========== AS27 ==========
        if (analizarSemantica) {
            if (prop.tipo.equals(VACIO) && listaPrima.tipo.equals(VACIO)) {
                lista.tipo = VACIO;
            } else {
                lista.tipo = ERROR_TIPO;
            }
        }
    }

    private void proposicion(Atributos prop) {
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
            Atributos proposicionCom = new Atributos();
            proposicion_compuesta(proposicionCom);
        } else if (preAnalisis.equals("if")) {
            // proposicion → if expresion then proposicion else proposicion
            emparejar("if");
            expresion();
            emparejar("then");
            proposicion(prop);
            emparejar("else");
            proposicion(prop);
        } else if (preAnalisis.equals("while")) {
            // proposicion → while expresion do proposicion
            emparejar("while");
            expresion();
            emparejar("do");
            proposicion(prop);
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
    private void expresion() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num")
                || preAnalisis.equals("(") || preAnalisis.equals("num.num")) {
            expresion_simple();
            _expresion();
        } else {
            error("[expresion] Se esperaba inicio de expresión.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void expresion_simple() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num")
                || preAnalisis.equals("(") || preAnalisis.equals("num.num")) {
            termino();
            _expresion_simple();
        } else {
            error("[expresion_simple] Se esperaba inicio de expresión simple.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void _expresion_simple() {
        if (preAnalisis.equals("opsuma")) {
            emparejar("opsuma");
            termino();
            _expresion_simple();
        } else if (preAnalisis.equals("oprel") || preAnalisis.equals(")")
                || preAnalisis.equals("]") || preAnalisis.equals("then")
                || preAnalisis.equals("do") || preAnalisis.equals(";")
                || preAnalisis.equals("end") || preAnalisis.equals("else")
                || preAnalisis.equals(",")) {
            // ε-producción
        } else {
            error("[expresion_simple’] Se esperaba 'opsuma' o fin de expresión simple.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void termino() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num")
                || preAnalisis.equals("(") || preAnalisis.equals("num.num")) {
            factor();
            _termino();
        } else {
            error("[termino] Se esperaba inicio de término.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void _termino() {
        if (preAnalisis.equals("opmult")) {
            emparejar("opmult");
            factor();
            _termino();
        } else if (preAnalisis.equals("opsuma") || preAnalisis.equals("oprel")
                || preAnalisis.equals(")") || preAnalisis.equals("]")
                || preAnalisis.equals("then") || preAnalisis.equals("do")
                || preAnalisis.equals(";") || preAnalisis.equals("end")
                || preAnalisis.equals("else") || preAnalisis.equals(",")) {
            // ε-producción
        } else {
            error("[termino’] Se esperaba 'opmult' o fin de término.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void factor() {
        if (preAnalisis.equals("id")) {
            emparejar("id");
            _factor();
        } else if (preAnalisis.equals("num")) {
            emparejar("num");
        } else if (preAnalisis.equals("num.num")) {
            emparejar("num.num");  // num.num
        } else if (preAnalisis.equals("(")) {
            emparejar("(");
            expresion();
            emparejar(")");
        } else {
            error("[factor] Se esperaba 'id', 'num' o '(' en factor.\nNo. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void _factor() {
        if (preAnalisis.equals("(")) {
            emparejar("(");
            lista_expresiones();
            emparejar(")");
        } else if (preAnalisis.equals("opmult") || preAnalisis.equals("opsuma")
                || preAnalisis.equals("oprel") || preAnalisis.equals(")")
                || preAnalisis.equals("]") || preAnalisis.equals("then")
                || preAnalisis.equals("do") || preAnalisis.equals(";")
                || preAnalisis.equals("end") || preAnalisis.equals("else")
                || preAnalisis.equals(",")) {
            // ε-producción
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
    
        private void _declaraciones_subprogramas(Atributos dspPrima) {

        // ======== caso ε ========  (AS15)
        if (!preAnalisis.equals("function") && !preAnalisis.equals("procedure")) {
            if (analizarSemantica) {
                dspPrima.tipo = VACIO;
            }
            return;
        }

        // ===== declaracion_subprograma ; declaraciones_subprogramas' =====
        Atributos dsub = new Atributos();
        Atributos dspPrima2 = new Atributos();

        declaracion_subprograma(dsub);
        emparejar(";");

        _declaraciones_subprogramas(dspPrima2);

        // ======== AS14 ========
        if (analizarSemantica) {
            if (dsub.tipo.equals(VACIO) && dspPrima2.tipo.equals(VACIO)) {
                dspPrima.tipo = VACIO;
            } else {
                dspPrima.tipo = ERROR_TIPO;
            }
        }
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
    private void P(Atributos atrPrograma) {
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
        fr = new FileReader(nomarchivo);
        br = new BufferedReader(fr);

        linea = br.readLine();

        while (linea != null) {

            // ===== Extraer nombre y tipo =====
            try { columna = linea.substring(0, 24).trim(); }
            catch (Exception e) { columna = "ERROR"; }

            try { tipo = linea.substring(29).trim(); }
            catch (Exception e) { tipo = "ERROR"; }

            try { ambito = nomarchivo.substring(0, nomarchivo.length() - 3); }
            catch (Exception e) { ambito = "ERROR"; }

            // Crear la linea para TS
            Linea_TS nueva = new Linea_TS(
                    "id",
                    columna,
                    "COLUMNA(" + tipo + ")",
                    ambito
            );

            // ===== Caso 1: Existe entrada con mismo lexema y ambito =====
            pos = cmp.ts.buscar(columna, ambito);
            if (pos > 0) {

                // Si no tiene tipo asignado → asignarlo
                if (cmp.ts.buscaTipo(pos).trim().isEmpty()) {
                    cmp.ts.anadeTipo(pos, "COLUMNA(" + tipo + ")");
                }

            } else {
                // ===== Caso 2: Existe lexema sin ambito =====
                pos = cmp.ts.buscar(columna);
                if (pos > 0) {
                    Linea_TS aux = cmp.ts.obt_elemento(pos);

                    if (aux.getAmbito().trim().isEmpty()) {

                        // Asignar tipo
                        cmp.ts.anadeTipo(pos, "COLUMNA(" + tipo + ")");

                        // Asignar ambito
                        cmp.ts.anadeAmbito(pos, ambito);

                    } else {
                        // Diferente ambito → insertar nueva entrada
                        cmp.ts.insertar(nueva);
                    }

                } else {
                    // ===== Caso 3: No existe en TS → insertar =====
                    cmp.ts.insertar(nueva);
                }
            }

            linea = br.readLine();
        }

        existeArch = true;

    } catch (IOException ex) {
        System.out.println(ex);
    } finally {
        try { if (br != null) br.close(); } catch (IOException ex) {}
        try { if (fr != null) fr.close(); } catch (IOException ex) {}
    }

    return existeArch;
}

    /*----------------------------------------------------------------------------------------*/

    
}
//------------------------------------------------------------------------------
//::
