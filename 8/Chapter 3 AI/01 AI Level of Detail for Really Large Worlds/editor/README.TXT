
Prerequisities
--------------
IVE Editor requires JDK 1.6 or higher. The compilation process is based on ant 1.7


Running IVE Editor
------------------
IVE Editor is distributed as compiled jar file which can be run by standard Java means.
To run the IVE, copy the directory editor to your harddrive and switch to the 
dist/lib directory containing the ive_editor.jar file. Run

java -jar ive_editor.jar

On Windows, you can alternatively run the jar file by clicking on its icon twice 
(if associations in your system are set correctly by Java installation)

In case of editor, it is important that the actual directory is dist/lib, since
the code assumes the "default" directory one level above the actual directory.


Compilation
-----------
To compile IVE Editor, switch to the project root directory containing the build.xml 
file and run:

ant



