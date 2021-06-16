/* 
 *
 * IVE - Inteligent Virtual Environment
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
 
package cz.ive.util;

import java.io.*;
import java.net.URLClassLoader;

/**
 * Helper object input stream, that can deserialize instances of the classes
 * not accesible from the current classpath but known to the specified
 * classloader.
 *
 * @author ondra
 */
public class IveObjectInputStream extends ObjectInputStream {
    
    /** ClassLoader to be used during the deserialization process */
    protected URLClassLoader loader;
    
    /**
     * Creates a new instance of IveObjectInputStream
     *
     * @param is InputStream from which to load.
     * @param loader ClassLoader to be used as a Source of class definitions.
     * @throws SecurityException Security manager exists and forbids subclassing
     *      of the ObjectInputStream
     * @throws IOException On IO problems when manipulating the stream.
     */
    public IveObjectInputStream(InputStream is, URLClassLoader loader) throws
            IOException, SecurityException {
        super(is);
        this.loader = loader;
    }
    
    /**
     * Resolves the class from the given class name using the classloader
     * specified in the constructor.
     *
     * @param desc Class description.
     * @return Class associated with the given description loaded by the class
     *      loader.
     * @throws IOException On IO problems when manipulating the stream.
     * @throws ClassNotFoundException When the class loader is unable to locate
     *      the corresponding class.
     */
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
            ClassNotFoundException {
        return loader.loadClass(desc.getName());
    }
    
    /**
     * Retrieves the IVE classloader.
     */
    public URLClassLoader getIveClassLoader() {
        return loader;
    }
}
