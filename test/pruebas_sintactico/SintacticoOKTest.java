///*:-----------------------------------------------------------------------------
// *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
// *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
// *:                         LENGUAJES Y AUTOMATAS II           
// *: 
// *:        SEMESTRE: ______________            HORA: ______________ HRS
// *:                                   
// *:               
// *:        # Casos de prueba JUnit  para el Analizador Sintactico                 
// *:                           
// *: Archivo       : SinctacticoOKTest.java
// *: Autor         : Fernando Gil   
// *: 
// *: Fecha         : 25/Feb/2024
// *: Compilador    : Java JDK 17
// *: Descripción   : Casos de prueba con programas correctamente escritos en 
// *:                 lenguajes SQLTec.   
// *:           	      
// *: Ult.Modif.    :
// *:  Fecha      Modificó            Modificacion
// *:=============================================================================
// *: 26/Feb/2024 FGil               -Se crearon los casos de prueba para SQLTec 2024  
// *:-----------------------------------------------------------------------------
// */
//
//package pruebas_sintactico;
//
//import compilador.Compilador;
//import java.util.ArrayList;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import static org.junit.Assert.*;
//
///**
// *
// * @author FGIL.0
// */
//public class SintacticoOKTest {
//    Compilador        cmp       = new Compilador ();
//    ArrayList<String> programas = new ArrayList<> ();
//
//    //--------------------------------------------------------------------------
//    
//    public SintacticoOKTest() {
//        
//        // #01-programaMinimo.sql
//        programas.add ( """
//            end 
//        """ );
//               
//        // #02-declaracionVariables.sql
//        programas.add ( """
//              declare @nombre_completo  char (30)
//              declare @promedio1        float
//              declare @base_men         int 
//              declare @nombre_completo2 char (10)
//              declare @promedio2        float
//              declare @base_men2        int   
//            end
//        """ );
//
//        // #03-sentenciasAsignacion.sql
//        programas.add ( """
//              assign @nombre_completo := 'Fernando'
//              assign @promedio1 := 0
//              assign @promedio1 := 3.14159  
//              assign @promedio1 := ( @calif1 + @calif2 ) * 20 
//            end  
//        """ );        
//  
//        // #04-sentenciaCondicional.sql
//        programas.add ( """
//              if @prom1 > 70   
//                begin
//                  assign @prom := 0
//                  print  @prom
//                end
//            
//              if @prom1 > 70 and @prom2 < 80
//                begin
//                  assign @prom := 0
//                  print  @prom
//                end
//              else
//                begin
//                  assign @prom := 0
//                  print  @prom
//                end
//            
//              case
//                when @min + 10 > @max + 20 then print 'dentro'
//                when @min < 50 then print 'fuera'
//                else print 'enmedio'
//              end 
//            end  
//        """ ); 
//
//        // #05-sentenciaRepetitiva.sql 
//        programas.add ( """
//            while @prom < 0  or @prom > 100
//                begin
//                  assign @prom := 0
//                  print  @prom      
//                end 
//            end
//        """ );         
//
//        // #06-Despliegue.sql 
//        programas.add ( """
//              print 'Hola ' + @nombre + 'como estas'
//              print @domicilio
//              print ( 10 + 3 ) * 5 
//            end
//        """ );  
//        
//        // #07-recuperacionDatos.sql 
//        programas.add ( """
//            select @codigo := codigoprod 
//             from   productos
//             where  precio > 10  
//
//             select @codigo := codigoprod, @preciounit := precio
//             from   productos
//             where  precio > 10 and precio < 20 
//
//             select @codigo := codigoprod, @preciounit := precio, @descrip := descripcion
//             from   productos
//             where  precio > 10 or precio < 20 
//
//           end  
//        """ );  
//        
//        // #08-eliminacionRegistros.sql 
//        programas.add ( """
//              delete from productos where precio >= 0
//              delete from productos where precio >= @promedio + 10 
//              delete from productos where precio >= @promedio + 10  and  codigo = 'maruchan'
//            end  
//        """ );  
//        
//        // #09-insercionRegistros.sql
//        programas.add ( """
//            insert into productos ( codigoprod ) values ( 10322 )
//
//             insert into productos ( codigoprod, descrip  ) values ( 10322, 'Sopa miserable' )
//
//
//             insert into productos ( codigoprod, descrip, precio1, precio2 )
//             values ( 10322, 'Sopa miserable', @prom * 10, precio1 + 20 )
//
//           end   
//        """ );  
//        
//        // #10-actualizacionRegistros.sql
//        programas.add ( """
//              update productos
//              set    descrip := 'Sopa maruchan' 
//              where  precio1 < @preciobase * 1.16
//            
//              update productos
//              set    descrip := 'Sopa maruchan', precio1 := 25
//              where  precio1 < precio2 
//              
//              update productos
//              set    descrip := 'Sopa maruchan', precio1 := 25, ubicacion := 'A32'
//              where  precio1 < precio2  or  precio1 = 10
//              
//            end  
//        """ );  
//        
//        // #11-creacionTablas.sql
//        programas.add ( """
//            create table articulos
//             ( IDARTICULO              int                not null,
//               NOMBRE                  char ( 30 )        not null,
//               DESCRIPCION             char ( 50 )        not null,
//               IDCATEGORIA             int                not null,
//               PRECIOPORUNIDAD         float              null,
//               NIVELREABASTECIMIENTO   int                null,
//               QOH                     int                null
//             ) 
//           end   
//        """ );  
//        
//        // #12-eliminacionTablas.sql
//        programas.add ( """
//            drop table articulos 
//           end   
//        """ );  
//        
//        // #13-programaPeligrosillo.sql
//        programas.add ( """
//            declare @num1 int
//             declare @nomb char ( 30 )
//             declare @prom float
//
//             assign @num1 := 100
//             assign @nomb := 'felipe'
//             assign @prom := ( @num1 + 3 ) * 20
//             print  'el promedio es : '
//             print  @prom
//
//             if @num1 > 0
//               begin
//                 print 'Hola Inges' + 'como estan'
//               end
//           end  
//        """ );  
//        
//        // #14-programaPeligroso.sql
//        programas.add ( """
//            select @nombre := nombreAlu, @numcontrol := matricula, @prom := promedio
//             from   alumnos
//             where  promedio > 70.0
//
//             delete from alumnos where nombreAlu = 'Taurino del Toro'
//
//             insert into alumnos ( nombreAlu, matricula, promedio ) values ( 'Gualter', 13023, 99.9 )
//
//             update alumnos
//             set calfinal := ( calif1 + calif2 + calif3 ), estatus := 'Aprobado'
//             where matricula = 12345
//
//           end  
//        """ );  
//        
//        // #15-programaMegaPeligroso.sql
//        programas.add ( """ 
//                          declare @nombre_completo  char (30)
//                          declare @promedio1        float
//                          declare @base_men         int 
//                          declare @nombre_completo2 char (10)
//                          declare @promedio2        float
//                          declare @base_men2        int  
//                          
//                          assign @nombre_completo := 'Fernando'
//                          assign @promedio1 := 0
//                          assign @promedio1 := 3.14159  
//                          assign @promedio1 := ( @calif1 + @calif2 ) * 20 
//                        
//                          if @prom1 > 70   
//                            begin
//                              assign @prom := 0
//                              print  @prom
//                            end
//                        
//                          if @prom1 > 70 and @prom2 < 80
//                            begin
//                              assign @prom := 0
//                              print  @prom
//                            end
//                          else
//                            begin
//                              assign @prom := 0
//                              print  @prom
//                            end
//                        
//                          case
//                            when @min + 10 > @max + 20 then print 'dentro'
//                            when @min < 50 then print 'fuera'
//                            else print 'enmedio'
//                          end 
//                        
//                          while @prom < 0  or @prom > 100
//                            begin
//                              assign @prom := 0
//                              print  @prom      
//                            end 
//                        
//                          print 'Hola ' + @nombre + 'como estas'
//                          print @domicilio
//                          print ( 10 + 3 ) * 5 
//                          
//                          select @codigo := codigoprod 
//                          from   productos
//                          where  precio > 10  
//                          
//                          select @codigo := codigoprod, @preciounit := precio
//                          from   productos
//                          where  precio > 10 and precio < 20 
//                          
//                          select @codigo := codigoprod, @preciounit := precio, @descrip := descripcion
//                          from   productos
//                          where  precio > 10 or precio < 20 
//                        
//                          delete from productos where precio >= 0
//                          delete from productos where precio >= @promedio + 10 
//                          delete from productos where precio >= @promedio + 10  and  codigo = 'maruchan'
//                        
//                          insert into productos ( codigoprod ) values ( 10322 )
//                        
//                          insert into productos ( codigoprod, descrip  ) values ( 10322, 'Sopa miserable' )
//                        
//                          insert into productos ( codigoprod, descrip, precio1, precio2 )
//                          values ( 10322, 'Sopa miserable', @prom * 10, precio1 + 20 )
//                        
//                          update productos
//                          set    descrip := 'Sopa maruchan' 
//                          where  precio1 < @preciobase * 1.16
//                        
//                          update productos
//                          set    descrip := 'Sopa maruchan', precio1 := 25
//                          where  precio1 < precio2 
//                          
//                          update productos
//                          set    descrip := 'Sopa maruchan', precio1 := 25, ubicacion := 'A32'
//                          where  precio1 < precio2  or  precio1 = 10
//                        
//                          create table articulos
//                          ( IDARTICULO              int                not null,
//                            NOMBRE                  char ( 30 )        not null,
//                            DESCRIPCION             char ( 50 )        not null,
//                            IDCATEGORIA             int                not null,
//                            PRECIOPORUNIDAD         float              null,
//                            NIVELREABASTECIMIENTO   int                null,
//                            QOH                     int                null
//                          ) 
//                          
//                          drop table articulos 
//                          
//                        end                         
//        """ );  
//        
//    }
//    
//    //--------------------------------------------------------------------------
//    
//    @BeforeClass
//    public static void setUpClass() {
//    }
//    
//    //--------------------------------------------------------------------------
//    
//    @AfterClass
//    public static void tearDownClass() {
//    }
//    
//    //--------------------------------------------------------------------------
//    
//    @Before
//    public void setUp() {
//    }
//    
//    //--------------------------------------------------------------------------
//    
//    @After
//    public void tearDown() {
//    }
//
//    //--------------------------------------------------------------------------
//    
//    @Test
//    public void programasOKTest () {
//
//        int i = 0;
//        // Por cada uno de los programas de prueba...
//        for ( String programa : programas ) { 
//            i++;
//            System.out.println ( "********* programasOKTest #" + i + " *********" );
//            
//            // Ejecutar el Lexico y Sintactico 
//            cmp.analizarLexico (programa);
//            cmp.analizarSintaxis();
//
//            // Debe contabilizar 0 errores y el primer error registrado debe ser vacio
//            assertEquals ( "programasOKTest #" + i, 
//                    0, cmp.getTotErrores ( Compilador.ERR_SINTACTICO ) );
//            assertEquals ( "programasOKTest #" + i, 
//                    "", cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO ) );
//        }
//    }  
//    
//    //--------------------------------------------------------------------------
//  
//}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pruebas_sintactico;

import compilador.Compilador;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author FGIL.0
 */
public class SintacticoOKTest {
    Compilador cmp = new Compilador ();
    
    public SintacticoOKTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    //--------------------------------------------------------------------------
    
    @Test
    public void programasOKTest () {
        ArrayList<String> programas = new ArrayList<> ();
        
        // #1 - Programa sin errores
        programas.add ( """
            program  program01 ( input, output );
            begin
            end .
        """ );
        
        // #2 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );
            var  a, b, c : integer;
            var  d       : real;
            var  e       : integer;
            var  g, h    : array [ 1 .. 12 ] of  real ;
            begin
            end .
        """ );

        // #3 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );

            procedure proc;
            begin
            end;

            procedure proc2 ( a : integer );
            begin
            end;

            function func : real;
            begin
            end ;

            function func2 ( a, b : integer; c : real ) : integer;
            begin
            end ;

            begin
            end .
        """ );
        
        // #4 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );

            procedure proc;
            var  a, b, c : integer;
            begin
            end;

            procedure proc2 ( a : integer );
            var  d       : real;
            var  e       : integer;
            begin
            end;

            function func : real;
            var  g, h    : array [ 1 .. 12 ] of  real ;
            begin
            end ;

            function func2 ( a, b : integer; c : real ) : integer;
            var  a, b, c : integer;
            var  d       : real;
            var  e       : integer;
            var  g, h    : array [ 1 .. 12 ] of  real ;
            begin
            end ;
                        
            begin
            end .
        """ );

        // #5 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );
            begin
              a := 882;
              b := 3.12123;
              c := 0;
              d := ( 2 * c + d ) * a + 1
            end .
        """ );

        // #6 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );
            begin
              a [ 1 ]                     := 882;
              b [ x ]                     := 3.12123;
              c [ x + 1 ]                 := 0;
              d [  2 * c + d  * a + 1 ]   := 0
            end .
        """ );
        
        // #7 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );
            begin
              a2       :=  promedio ( a, b, 3 );
              a [ 1 ]  :=  1 + promedio ( a, b, 3 )
            end .
        """ );       
        
        // #8 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );
            begin
              if a < 10 then
               begin
                    a := 1 ;
                    b := 1.2
               end 
              else
               begin
                    a := 1 ;
                    b := 1.2
               end
            end .
        """ ); 

        // #9 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );
            begin
              while a < 10 do
               begin
                 while a + 1 * y < 2 * ( b + 3 + 1 ) do
                    begin 
                    end
               end 
            end .
        """ ); 

        // #10 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );
            procedure proc;
            begin
              d := ( 2 * c + d ) * a + 1
            end;

            procedure proc2 ( a : integer );
            begin
              d := ( 2 * c + d ) * a + 1;
              d [  2 * c + d  * a + 1 ]   := 0
            end;

            function func : real;
            begin
              a2       :=  promedio ( a, b, 3 );
              a [ 1 ]  :=  1 + promedio ( a, b, 3 )
            end ;

            function func2 ( a, b : integer; c : real ) : integer;
            begin
              if a < 10 then
               begin
                 a := 1 ;
                 b := 1.2
               end 
              else
               begin
                 a := 1 ;
                 b := 1.2
               end ;

              while a < 10 do
               begin
                 while a + 1 * y < 2 * ( b + 3 + 1 ) do
                    begin 

                    end
               end    
            end ;

            begin
            end .
        """ ); 

        // #11 - Programa sin errores
        programas.add ( """
            program program01 ( input, output );
            var  a, b, c : integer;
            var  d       : real;
            var  e       : integer;
            var  g, h    : array [ 1 .. 12 ] of  real ;

            procedure proc;
            begin
              d := ( 2 * c + d ) * a + 1
            end;

            procedure proc2 ( a : integer );
            begin
              d := ( 2 * c + d ) * a + 1;
              d [  2 * c + d  * a + 1 ]   := 0
            end;

            function func : real;
            begin
              a2       :=  promedio ( a, b, 3 );
              a [ 1 ]  :=  1 + promedio ( a, b, 3 )
            end ;

            function func2 ( a, b : integer; c : real ) : integer;
            begin
              if a < 10 then
               begin
                 a := 1 ;
                 b := 1.2
               end 
              else
               begin
                 a := 1 ;
                 b := 1.2
               end ;

              while a < 10 do
               begin
                 while a + 1 * y < 2 * ( b + 3 + 1 ) do
                    begin 

                    end
               end    
            end ;

            begin
              a := 882;
              b := 3.12123;
              c := 0;
              d := ( 2 * c + d ) * a + 1;  
              a [ 1 ]                     := 882;
              b [ x ]                     := 3.12123;
              c [ x + 1 ]                 := 0;
              d [  2 * c + d  * a + 1 ]   := 0;
              a2       :=  promedio ( a, b, 3 );
              a [ 1 ]  :=  1 + promedio ( a, b, 3 );
              if a < 10 then
               begin
                 a := 1 ;
                 b := 1.2
               end 
              else
               begin
                 a := 1 ;
                 b := 1.2
               end ;

              while a < 10 do
               begin
                 while a + 1 * y < 2 * ( b + 3 + 1 ) do
                    begin 

                    end
               end 
            end .
        """ ); 
        
        int i = 0;
        // Por cada uno de los programas de prueba...
        for ( String programa : programas ) { 
            i++;
            System.out.println ( "********* programasOKTest #" + i + " *********" );
            
            // Ejecutar el Lexico y Sintactico 
            cmp.analizarLexico (programa );
            cmp.analizarSintaxis();

            // Debe contabilizar 0 errores y el primer error registrado debe ser "" 
            assertEquals ( "programasOKTest #" + i, 
                    0, cmp.getTotErrores ( Compilador.ERR_SINTACTICO ) );
            assertEquals ( "programasOKTest #" + i, 
                    "", cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO ) );
        }
    }  
    
    //--------------------------------------------------------------------------
  
}