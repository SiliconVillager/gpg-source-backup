/* 
 *
 * IVE Editor 
 * Copyright (c) 2005-2009, IVE Team, Charles University in Prague
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, 
 *       this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the Charles University nor the names of its contributors 
 *       may be used to endorse or promote products derived from this software 
 *       without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 *
 */
 
package IVE_Editor;

import IVE_Editor.PlugIns.Components.Project.Project;
import IVE_Editor.PlugIns.PlugIn;
import org.omg.SendingContext.RunTime;

/**
 * 
 * @author Jirka
 */  
public class Main {    
    
    private static final int P = 2;
    private static final int R = 4;
    private static final int W = 8;
    private static final int H = 16;    
    
    private static int _flags = 0;

    /** Filename from command line. */
    private static String _filename = "";   
    
    /** Name of new project from command line. */
    private static String _project_name = "";
    
    /** Creates a new instance of Main */   
    public Main() { 
    }        
         
    /** 
     * @param args the command line arguments
     */
    public static void main( String [] args ) {
        //Command debug parse
//        String arg = null;
          boolean start_gui = true;
//        for ( int i = 0; i < args.length; i++ )
//        {
//            char ch =  args[i].charAt(0);
//            if ( args[i].charAt(0) == '-' )
//            {
//                if ( 1 < args[i].length() )
//                    if ( args[i].substring( 1 ).equals( "ng" ) )
//                        start_gui = false;                
//            }
//        }                      
        
        parse_command_line( args );        
        
        //debug command line arguments
//        debug_cmd();
        
        GUI gui = new GUI();
        final PlugInsLoader loader = new PlugInsLoader( gui );
        loader.loadModuls();   //nahraje moduly uvedene v loaderu do aplikace   
        
//        start_gui = true; //docasne nasilne prepinani pro ladeni, aby jsem nemusel porad menit vstupni argumenty
        
        if ( start_gui == true ){
            //application is run in normal mode with GUI
            gui.initGUI();
        }
        else gui.initWithoutGUI();
        
        obey_cmd( loader );        
        
    }
    
    /** 
     *  Parse command line arguments and sets the global array _flags 
     *  of argument flags and propably the _filename field. When it finds
     *  some inappropriate arguments then it terminates the application
     *  with an exit code 1.
     */
    private static void parse_command_line( String [] args ) {
        for ( int i = 0; i < args.length; i++ ) {
            
            if ( args[ i ].equals( "-p" ) ||
                 args[ i ].equals( "-pr" ) ){                
                _flags |= P;                
                if ( i == args.length - 1 )
                    error( "Missing path to the project. " + "\n" + get_help() );                               
                if ( args[ i ].equals( "-pr" ) )
                    _flags |= R;                
                _filename = args[ ++i ];
                               
            } else if ( args[ i ].equals( "-w" ) ) {
                _flags |= W;                
                if ( i >= args.length - 2 )
                    error( "Missing arguments. " + "\n" + get_help() );
                _project_name = args[ ++i ];
                _filename = args[ ++i ];                
            } else if ( args[ i ].equals( "-h" ) ||
                        args[ i ].equals( "--help" ) ) {
                _flags |= H;
            } else
                error( get_help() );            
        }
        
        //propably print the help page
        if ( (_flags & H) != 0 )
            System.out.println( get_help() );        
        if ( (_flags & ~H) == 0 && (_flags & H) != 0 )
            Runtime.getRuntime().exit( 0 );        
    }
    
    private static void error( String error_msg ) {
        if ( error_msg == null )
            return;
        System.out.println( error_msg );
        Runtime.getRuntime().exit(1);
    }
    
    private static String get_help() {
        String help_page = "";
        help_page += "Inteligent Virtual Environment Editor - IVE Editor" +
                "\n\n  Command line arguments:\n";
        help_page += "\t-p [project name]\tLoad the specified project.\n";
        help_page += "\t-pr [project name]\tLoad the specified project and" +
                " try to run it in IVE.\n";
        help_page += "\t-w [new project name][XML filename]\tLoad the specified world into" +
                " a new project.\n";
        help_page += "\t-h --help\t\tView the help page (this page).";        
        return help_page;
    }
    
    private static void debug_cmd(){
        System.out.println("Arguments were:\n");
        if ( (_flags & P) != 0 )
            System.out.println("-p " + _filename + "\n");
        if ( (_flags & (P|R)) == (P|R) )
            System.out.println("-pr " + _filename + "\n");
        if ( (_flags & R) != 0 && (_flags & P) == 0 )
            System.out.println("some strange error\n");        
        if ( (_flags & W) != 0 )
            System.out.println("-w " + _project_name + _filename + "\n" );
        if ( (_flags & H) != 0 )
            System.out.println("-h\n");        
        Runtime.getRuntime().exit( 0 );        
    }
    
    private static void obey_cmd( final PlugInsLoader loader ) {
        if ( loader == null )
            return;
        
        if ( (_flags & P) != 0 ) {
            PlugIn pl = loader.getPlugIn( loader.Project );
            
            if ( pl == null )
                return;
            
            if ( !( pl instanceof Project ) )
                return;
            
            ((Project)pl).openProjectByPath( _filename );
            
            if ( (_flags & R) != 0 ) { 
                ((Project)pl).runInIveAction();
            }
        } else if ( (_flags & W) != 0  ) {
            PlugIn pl = loader.getPlugIn( loader.Project );
            
            if ( pl == null )
                return;
            
            if ( !( pl instanceof Project ) )
                return;
            
            ((Project)pl).newProjectAction( _filename, _project_name );
        } 
    }
    
} 
