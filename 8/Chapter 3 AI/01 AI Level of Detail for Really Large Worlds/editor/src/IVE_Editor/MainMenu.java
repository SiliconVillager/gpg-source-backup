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

import java.util.ArrayList;
import java.util.Stack;
import javax.swing.*;

/**
 *
 * @author Jirka
 */
public class MainMenu {
    
    private JMenuBar menuBar;
    private Stack< JComponent > stack;
    
    public static final String [] pattern = { "File", "MPC", "Settings", "Windows" };
    
    private JMenu [] orderList = new JMenu[ pattern.length ];
    
    private ArrayList< JMenu > buffer = new ArrayList< JMenu >();
    
    /** Creates a new instance of MainMenu */
    public MainMenu() {
        menuBar = new JMenuBar();
        stack = new Stack< JComponent >();
    }
    
    public JMenuBar getBar()
    {
        return menuBar;
    }
    
//    /** pøidá do baru menu */
//    public void add( JMenu menu )
//    {
//        menuBar.add( menu );
//    }

//    /**  pøidá do baru menu za menu s názvem menuName */
//    public void add( String menuName, JMenu menu )
//    {
//        int i;
//        for( i = 0; i < menuBar.getMenuCount() && menuBar.getMenu( i ).getText() != menuName; i++ );
//        if ( i < menuBar.getMenuCount() ) menuBar.add( menu, i + 1 );
//        //Mohlo by tady hodit vyjimku informjici o tom, ze pozadovana polozka neni v menu
//   }
    
//    /**  pøidá do baru menu za menu s názvem menuName */
//    public void insert( int pos , JMenu menu )
//    {
//        menuBar.add( menu, pos );
//   }
    
    /**  pøidá do menu položku item */    
    public void addToMenu( JMenu menu, JMenuItem item )
    {
        menu.add( item );
    }
   
    /** 
     *  Find the position for menu with specified caption according to
     *  static pattern.
     *
     *  @return int position of the menu with specified caption in the menu bar.
     *  The position is greater then pattern.length in case of not being present 
     *  in the pattern.
     */
    private int getIndex( String caption ) {
        int i;
        
        for ( i = 0; i < pattern.length; ++i ) {
            if ( caption.equals( pattern[i] ) )
                return i;
        }
        
        return i;
    }
    
    /** 
     *  Add menu into the menu bar according to static pattern. If caption
     *  of the menu isn't specified in the pattern then the menu is added
     *  at the end of the menu bar.
     */
    public void addMenu( JMenu menu ) {
        int i = getIndex( menu.getText() );
        if ( i >= pattern.length )
            buffer.add( menu );
        else
            orderList[i] = menu;
        
        initMenu();
    }
    
    /** */
    private void initMenu() {
        menuBar.removeAll();
        
        for ( int i = 0; i < pattern.length; ++i ) {
            if ( orderList[i] != null )
                menuBar.add( orderList[i] );
        }
        
        for ( JMenu m : buffer )
            menuBar.add( m );        
                   
        menuBar.revalidate();
        menuBar.repaint();
    }
    
    /**  pøidá do menu s názvem menuName položku item */
    public void addToMenu( String menuName, JMenuItem item )
    {
        int i;
        for( i = 0; i < menuBar.getMenuCount() && menuBar.getMenu( i ).getText() != menuName; i++ );
        if( i < menuBar.getMenuCount() ) menuBar.getMenu( i ).add( item );
    }
    
    /**  pøidá do menu s názvem menuName položku item na danou pozici */
    public void insertToMenu( String menuName, JMenuItem item, int pos )
    {
        int i;
        for( i = 0; i < menuBar.getMenuCount() && menuBar.getMenu( i ).getText() != menuName; i++ );
        if( i < menuBar.getMenuCount() ) menuBar.getMenu( i ).add( item, pos );
    }
    
    public void insertToMenuTemp( String menuName, JSeparator sep, int pos )
    {
        int i;
        for( i = 0; i < menuBar.getMenuCount() && menuBar.getMenu( i ).getText() != menuName; i++ );
        if( i < menuBar.getMenuCount() )
        {
            menuBar.getMenu( i ).add( sep, pos );
            stack.push( sep );
        }        
    }    
    
    public void insertToMenuTemp( String menuName, JMenuItem item, int pos )
    {
        int i;
        for( i = 0; i < menuBar.getMenuCount() && menuBar.getMenu( i ).getText() != menuName; i++ );
        if( i < menuBar.getMenuCount() )
        {
            menuBar.getMenu( i ).add( item, pos );
            stack.push( item );
        }        
    }    
    
    /** pøidá do baru menu */
    public void addTemp( JMenu menu )
    {
        //add( menu );
        stack.push( menu );        
        addMenu( menu );//new
        //menuBar.revalidate(); 
    }

//    /**  pøidá do baru menu za menu s názvem menuName */
//    public void addTemp( String menuName, JMenu menu )
//    {
//        add( menuName, menu );
//        stack.push( menu );
//        menuBar.revalidate();
//    }
    
//    /**  pøidá do baru menu za menu s názvem menuName */
//    public void insertTemp( int pos , JMenu menu )
//    {
//        insert( pos, menu );
//        stack.push( menu );        
//    }
    
   
    public void removeTemp()
    {
        while( !stack.empty() ) 
        {
            for( int i = 0; menuBar.getMenuCount() > i; i++ )
                menuBar.getMenu( i ).remove( stack.peek() );
            
            for ( int i = 0; i < pattern.length; ++i )//new
                if ( orderList[i] == stack.peek() )
                    orderList[i] = null;
            
            buffer.remove( stack.peek() ); //new
            
            menuBar.remove( stack.pop() );
        }
        initMenu();//new
//        menuBar.revalidate();
//        menuBar.repaint();
    }
       
}
