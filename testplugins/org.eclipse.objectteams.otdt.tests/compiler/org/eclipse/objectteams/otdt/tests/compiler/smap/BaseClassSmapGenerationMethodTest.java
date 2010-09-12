/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BaseClassSmapGenerationMethodTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.smap;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import junit.framework.TestCase;

/** Tests the Method which generates the Smap for Baseclasse.
 * @author ike
 */
public class BaseClassSmapGenerationMethodTest extends TestCase
{
    
    public void testMethod001()
    {
        String baseClassFileName = "SimpleClass$1" ;
        String baseClassSourceUnit = "path/to/SimpleClass"; 
        int endlineNumber = 3;
        Hashtable<String, int[][]> mappings = new Hashtable<String, int[][]>();
        int lineMappings[][] = new int[2][3];
        lineMappings[0] = new int[]{2,2,7};
        lineMappings[1] = new int[]{3,0,8};
               
        mappings.put("path/to/TeamA.java", lineMappings);
        
        int baseMapping [][] = null;
        
        String expectedString = 
            "SMAP" + "\n" +
            "SimpleClass$1.class" + "\n" +
            "OTJ" + "\n" +
            "*S OTJ" + "\n" +
            "*F" + "\n" +
            "+ 1 TeamA.java" + "\n" +
            "path/to/TeamA.java" + "\n" +
            "+ 2 SimpleClass.java" + "\n" +
            "path/to/SimpleClass.java" + "\n" +
            "*L" + "\n" +
            "1#2:1" + "\n" +
            "2#1,2:7" + "\n" +
            "3#1:8" + "\n" +
            "*E"
            ;
        
        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        
        String actualString = new String(actual);
        
        assertEquals("SMAP should be equal.", expectedString, actualString);
    }
    
    public void testMethod002()
    {
        String baseClassFileName = "SimpleClass$1" ;
        String baseClassSourceUnit = "SimpleClass"; 
        int endlineNumber = 3;
        Hashtable<String, int[][]> mappings = new Hashtable<String, int[][]>();
        int lineMappings[][] = new int[2][3];
        lineMappings[0] = new int[]{2,2,7};
        lineMappings[1] = new int[]{3,0,8};
        
        mappings.put("TeamA.java", lineMappings);
        
        int baseMapping [][] = null;
        
        String expectedString =
            "SMAP" + "\n" +
            "SimpleClass$1.class" + "\n" +
            "OTJ" + "\n" +
            "*S OTJ" + "\n" +
            "*F" + "\n" +
            "1 TeamA.java" + "\n" +
            "2 SimpleClass.java" + "\n" +
            "*L" + "\n" +
            "1#2:1" + "\n" +
            "2#1,2:7" + "\n" +
            "3#1:8" + "\n" +
            "*E";
        
        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        
        String actualString = new String(actual);
        
        assertEquals("SMAP should be equal.", expectedString, actualString);
        
    }    
    
    public void testMethod003()
    {
        String baseClassFileName = "SimpleClass$1" ;
        String baseClassSourceUnit = "SimpleClass"; 
        int endlineNumber = 3;
        Hashtable<String, int[][]> mappings = new Hashtable<String, int[][]>();
        
        int baseMapping [][] = null;
        
        String expectedString =
            "SMAP" + "\n" +
            "SimpleClass$1.class" + "\n" +
            "OTJ" + "\n" +
            "*S OTJ" + "\n" +
            "*F" + "\n" +
            "1 SimpleClass.java" + "\n" +
            "*L" + "\n" +
            "1#1,3:1" + "\n" +
            "*E";
        
        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        
        String actualString = new String(actual);
        
        assertEquals("SMAP should be equal.", expectedString, actualString);
        
    }
    
    public void testMethod004()
    {
        String baseClassFileName = "BaseClass" ;
        String baseClassSourceUnit = "very/very/long/path/to/BaseClass"; 
        int endlineNumber = 50;
        Hashtable<String, int[][]> mappings = new Hashtable<String, int[][]>();
        int lineMappings1[][] = new int[7][3];
        lineMappings1[0] = new int[]{7,0,10};
        lineMappings1[1] = new int[]{14,1,25};
        lineMappings1[2] = new int[]{21,0,30};
        lineMappings1[3] = new int[]{28,2,35};
        lineMappings1[4] = new int[]{35,0,40};
        lineMappings1[5] = new int[]{42,3,45};
        lineMappings1[6] = new int[]{125,0,250};
        
        mappings.put("path/to/TeamB.java", lineMappings1);
        
        int lineMappings2[][] = new int[4][3];
        lineMappings2[0] = new int[]{8,0,3};
        lineMappings2[1] = new int[]{16,1,9};
        lineMappings2[2] = new int[]{24,0,12};
        lineMappings2[3] = new int[]{32,5,15};
        
        mappings.put("very/long/path/to/TeamA.java", lineMappings1);
        
        int baseMapping [][] = null;
        
        String expectedString =
            "SMAP" + "\n" +
            "BaseClass.class" + "\n" +
            "OTJ" + "\n" +
            "*S OTJ" + "\n" +
            "*F" + "\n" +
            "+ 1 TeamA.java" + "\n" +
            "very/long/path/to/TeamA.java" + "\n" +
            "+ 2 TeamB.java" + "\n" +
            "path/to/TeamB.java" + "\n" +
            "+ 3 BaseClass.java" + "\n" +
            "very/very/long/path/to/BaseClass.java" + "\n" +
            "*L" + "\n" +
            "1#3,6:1" + "\n" +
            "7#2:10" + "\n" +
            "8#3,6:8" + "\n" +
            "14#2,1:25" + "\n" +
            "15#3,6:15" + "\n" +
            "21#2:30" + "\n" +
            "22#3,6:22" + "\n" +
            "28#2,2:35" + "\n" +
            "29#3,6:29" + "\n" +
            "35#2:40" + "\n" +
            "36#3,6:36" + "\n" +
            "42#2,3:45" + "\n" +
            "43#3,8:43" + "\n" +
            "125#2:250" + "\n" +
            "*E";        
        
        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        
        String actualString = new String(actual);
        
        assertEquals("SMAP should be equal.", expectedString, actualString);
        
    }
    
    public void testSMAPIsNull001()
    {
        String baseClassFileName = "SimpleClass$1" ;
        String baseClassSourceUnit = "path/to/SimpleClass";
        int endlineNumber = 3;
        Hashtable<String, int[][]> mappings = null;
        
        int baseMapping [][] = null;
        
        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        
        assertNull("No SMAP should be generated.", actual);
    } 
    
    public void testSMAPIsNull002()
    {
        String baseClassFileName = null;
        String baseClassSourceUnit = "path/to/SimpleClass";
        int endlineNumber = 3;
        Hashtable<String, int[][]> mappings =  new Hashtable<String, int[][]>();;
        int lineMappings[][] = new int[2][3];
        lineMappings[0] = new int[]{2,2,7};
        lineMappings[1] = new int[]{3,0,8};
        mappings.put("path/to/TeamA.java", lineMappings);
        
        int baseMapping[][] = null;
        
        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        
        assertNull("No SMAP should be generated.", actual);
    }    
    
    public void testSMAPIsNotNull001()
    {
        String baseClassFileName = "SimpleClass$1";
        String baseClassSourceUnit = null;
        int endlineNumber = 3;
        Hashtable<String, int[][]> mappings =  new Hashtable<String, int[][]>();;
        int lineMappings[][] = new int[2][3];
        lineMappings[0] = new int[]{2,2,7};
        lineMappings[1] = new int[]{3,0,8};
        mappings.put("path/to/TeamA.java", lineMappings);
        
        int baseMapping[][] = null;
        
        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        
        String expectedString =
            "SMAP" + "\n" +
            "SimpleClass$1.class" + "\n" +
            "OTJ" + "\n" +
            "*S OTJ" + "\n" +
            "*F" + "\n" +
            "+ 1 TeamA.java" + "\n" +
            "path/to/TeamA.java" + "\n" +
            "2 SimpleClass$1.java" + "\n" +
            "*L" + "\n" +
            "1#2:1" + "\n" +
            "2#1,2:7" + "\n" +
            "3#1:8" + "\n" +
            "*E";
        
        
        String actualString = new String(actual);
        
        assertEquals("SMAP should be equal.", expectedString, actualString);
        
    } 
    
    public void testSMAPIsNotNull002()
    {
        String baseClassFileName = "pkg.subpkg.SimpleClass$1";
        String baseClassSourceUnit = null;
        int endlineNumber = 3;
        Hashtable<String, int[][]> mappings =  new Hashtable<String, int[][]>();;
        int lineMappings[][] = new int[2][3];
        lineMappings[0] = new int[]{2,2,7};
        lineMappings[1] = new int[]{3,0,8};
        mappings.put("path/to/TeamA.java", lineMappings);
        
        int baseMapping [][] = null;
        
        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        
        String expectedString =
            "SMAP" + "\n" +
            "SimpleClass$1.class" + "\n" +
            "OTJ" + "\n" +
            "*S OTJ" + "\n" +
            "*F" + "\n" +
            "+ 1 TeamA.java" + "\n" +
            "path/to/TeamA.java" + "\n" +
            "+ 2 SimpleClass$1.java" + "\n" +
            "pkg/subpkg/SimpleClass$1.java" + "\n" +
            "*L" + "\n" +
            "1#2:1" + "\n" +
            "2#1,2:7" + "\n" +
            "3#1:8" + "\n" +
            "*E";
        
        
        String actualString = new String(actual);
        
        assertEquals("SMAP should be equal.", expectedString, actualString);
        
    }
    
    
    public void testSMAPBaseMappings()
    {
        String baseClassFileName = "path.to.SimpleClass";
        String baseClassSourceUnit = "path/to/SimpleClass";
        int endlineNumber = 15;
        Hashtable<String, int[][]> mappings =  new Hashtable<String, int[][]>();;
        int lineMappings[][] = new int[2][3];
        lineMappings[0] = new int[]{2,2,7};
        lineMappings[1] = new int[]{3,0,8};
        mappings.put("path/to/TeamA.java", lineMappings);
        
        int baseMapping[][] = new int[2][2];
        baseMapping[0] = new int[]{10,1};
        baseMapping[1] =  new int[]{12,6};

        byte actual[] = generateSMAP(baseClassFileName, baseClassSourceUnit, endlineNumber, mappings, baseMapping);
        String actualString = new String(actual);
        
        String expectedString =
            "SMAP" + "\n" +
            "SimpleClass.class" + "\n" +
            "OTJ" + "\n" +
            "*S OTJ" + "\n" +
            "*F" + "\n" +
            "+ 1 TeamA.java" + "\n" +
            "path/to/TeamA.java" + "\n" +
            "+ 2 SimpleClass.java" + "\n" +
            "path/to/SimpleClass.java" + "\n" +
            "*L" + "\n" +
            "1#2:1" + "\n" +
            "2#1,2:7" + "\n" +
            "3#1:8" + "\n" +
            "4#2,6:4" + "\n" +
            "10#2:1" + "\n" +
            "11#2:11" + "\n" +
            "12#2:6" + "\n" +
            "13#2,3:13" + "\n" +
            "*E";
        
        assertEquals("SMAP should be equal.", expectedString, actualString);
    }  
    
    ///////////////////////////////////////////////////////////////////////////////////////
    ////                             THIS METHOD IS TESTED                             ////
    ///////////////////////////////////////////////////////////////////////////////////////
    
    /** This method generates a SMAP for Baseclasses. This SMAP maps generated code for
     *  callin bindings to theirs corresponding sourcecodepresentation. 
     * 
     * @param baseClassfileName - name of classfile this smap is generated for (e.g. pkg.subpkg.SimpleClass)
     * @param baseSourceunitName - path of sourceunit of corresponding baseclasstype, 
     *                             (e.g path/to/SimpleClass); 
     * 							   if  baseSourceunitName is not available name and path are generated from
     *                             baseClassfileName
     *                               
     * @param endlineNumber - this linenumber is the maximum linenumber of all entries in linenumbertables of all methods;
     *                        it is not the endlinenumber of sourcefile.
     * @param mappings - 
     * String -> int[][]<br>
     * key: path of Team or RoleFile sourceunit which has callins"<br>
     * value: int-array with:<br> int[0][0] - inputstartline of callin<br>
     * 						 int[0][1] - offset, if callin has more as one line(0 means one line, 1 means 2 lines, ...)<br>   
     * 	 				 	 int[0][2] - written linenumber in baseclasscode (linenumber of _OT$MyRole$roleMethod$baseMethod() call in chainMethod)<br> 
     * e.g.<br>
     * path/to/Teamname.java -> <br>[9][0][16]<br> [20][2][18]
     * 
     * @param baseMappings - contains mappings to itself (int[][]):<br>
     * 						int[0][0] - generated linenumber<br>
     * 						int[0][1] - fixed linenumber (linenumber of e.g. signature of "_OT$xyz$orig(..)")
     * 
     * @return smap - the string which should be stored in Classfileattribute SourceDebugExtension
     * 
     * @author ike
     */
    public byte[] generateSMAP(String baseClassfileName, String baseSourceunitName, int endlineNumber, Hashtable<String, int[][]> mappings, int baseMappings [][])
    {
        if (baseClassfileName==null || mappings==null)
        {
            return null;
        }
        
        String OTJ_STRATUM_NAME = "OTJ";
        String CLASS_ENDING = ".class";
        String JAVA_ENDING = ".java";
        String generatedFileName = new String();
        String components[] =  baseClassfileName.split("\\.");
        String newBaseSourceunitName = null;
        String absoluteBaseSourceunitName = null;
        boolean baseSourceunitNameAvailable = (baseSourceunitName != null);
        
        if (components.length > 1)
        {
            generatedFileName = components[components.length -1] + CLASS_ENDING;
            newBaseSourceunitName = components[components.length -1] + JAVA_ENDING;
            absoluteBaseSourceunitName = (baseClassfileName.replaceAll("\\.", "/") + JAVA_ENDING);
        }
        else
        {
            generatedFileName = baseClassfileName + CLASS_ENDING;
            newBaseSourceunitName = baseClassfileName + JAVA_ENDING;
            absoluteBaseSourceunitName = null;
        }

        if (baseSourceunitNameAvailable)
        {
            String tmp[] = baseSourceunitName.split("/");
            int index = tmp.length;
            
            newBaseSourceunitName = tmp[index-1] + JAVA_ENDING;
            
            if (tmp.length > 1)
            {
                absoluteBaseSourceunitName = baseSourceunitName + JAVA_ENDING;                
            }
        }
        
        Hashtable<String, Integer> typeNameToFileId = new Hashtable<String, Integer>();
        int fileSectionIdCounter = 1;
        
        StringBuffer out = new StringBuffer();
        
        // print Header
        out.append("SMAP\n");
        out.append(generatedFileName + "\n");
        
        // print strata
        out.append(OTJ_STRATUM_NAME+ "\n");
        
        // begin StratumSection
        out.append("*S " + OTJ_STRATUM_NAME + "\n");
        
        // print FileSection
        out.append("*F" + "\n");
        
        for (Iterator<String> iter = mappings.keySet().iterator(); iter.hasNext();)
        {
            String fullqualifiedName = iter.next();
            typeNameToFileId.put(fullqualifiedName, new Integer(fileSectionIdCounter));
            
            // extract filename and absoluteFileName from typename
            String tmp[] = fullqualifiedName.split("/");
            int index = tmp.length;
            
            String fileName = tmp[index-1];
            String absoluteFileName = null;
            
            typeNameToFileId.put(fileName, new Integer(fileSectionIdCounter));
            
            if (tmp.length >= 2)
            {
                absoluteFileName = fullqualifiedName;
            }
            
            if (absoluteFileName != null)
            {
                out.append("+ " );
            }
            
            out.append(fileSectionIdCounter + " " + fileName + "\n");
            
            if (absoluteFileName != null)
            {
                out.append(absoluteFileName + "\n");
            }
            
            fileSectionIdCounter++;
        }
        
        // add basename to smap
        int baseClassFileId = -1;
        typeNameToFileId.put(newBaseSourceunitName, new Integer(fileSectionIdCounter));
        baseClassFileId = typeNameToFileId.get(newBaseSourceunitName).intValue();
        
        if (absoluteBaseSourceunitName != null)
        {
            out.append("+ " );
        }
        
        out.append(baseClassFileId + " " + newBaseSourceunitName + "\n");
        
        if (absoluteBaseSourceunitName != null)
        {
            out.append(absoluteBaseSourceunitName + "\n");
        }
        
        
        // print LineSection
        out.append("*L" + "\n");
        
        TreeMap<Integer, String> lineInfos = new TreeMap<Integer, String>();
        StringBuffer lineBuffer = new StringBuffer();
        for (Iterator<String> iter = mappings.keySet().iterator(); iter.hasNext();)
        {
            String typeName = iter.next();
            int[][] linemappings = mappings.get(typeName);
            int fileId = typeNameToFileId.get(typeName).intValue();
            
            for (int idx = 0; idx < linemappings.length; idx++)
            {
                int[] singleLineMapping = linemappings[idx];
                
                Integer inputStartLine = new Integer( singleLineMapping[0]);
                int inputOffset = singleLineMapping[1];
                int outputLine = singleLineMapping[2];
                
                if (inputOffset == 0)
                {
                    lineBuffer.append(inputStartLine.intValue());
                    lineBuffer.append("#" + fileId);
                    lineBuffer.append(":" + outputLine + "\n");
                }
                else
                {
                    lineBuffer.append(inputStartLine.intValue());
                    lineBuffer.append("#" + fileId);
                    lineBuffer.append("," + inputOffset);
                    lineBuffer.append(":" + outputLine + "\n");
                }
                
                lineInfos.put(inputStartLine, lineBuffer.toString());
                lineBuffer.delete(0, lineBuffer.length());
            }
        }
        
        //add given mappings to itself (basemappings)
        if (baseMappings != null && baseMappings.length > 0)
        {
            for (int idx = 0; idx < baseMappings.length; idx++)
            {
                int key = baseMappings[idx][0];
                int value = baseMappings[idx][1];

                lineBuffer.append(key);
                lineBuffer.append("#" + baseClassFileId);
                lineBuffer.append(":" + value + "\n");
                
                lineInfos.put(new Integer(key), lineBuffer.toString());
                lineBuffer.delete(0, lineBuffer.length());
            }
        }
        
        //add mappings to itself, 
        //endlinenumber is the maximum linenumber of all entries in linenumbertables of all methods;
        //endlinenumber is not the endlinenumber of sourcefile, cause it is available at this time
        TreeMap<Integer, int[]> baseToBaselineInfos = new TreeMap<Integer, int[]>();
        for (int idx = 1; idx <= endlineNumber; idx++)
        {
        	Integer currentLineNumber = new Integer(idx);
            if (!lineInfos.containsKey(currentLineNumber))
            {
            	Integer previousLineNumber = currentLineNumber-1;
            	if (baseToBaselineInfos.containsKey(previousLineNumber))
            	{
            		int[] oldValues = baseToBaselineInfos.get(previousLineNumber);
            		int intputStartline = oldValues[0];
            		int repeatCount= oldValues[1]+1;
            		int newValues[] = {intputStartline, repeatCount};
            		baseToBaselineInfos.put(currentLineNumber, newValues);
            	}
            	else
            	{
            		baseToBaselineInfos.put(currentLineNumber, new int[]{currentLineNumber.intValue(), 1});
            	}
            }
        }
        
        lineBuffer = new StringBuffer();        
        for (Iterator<Integer> iter = baseToBaselineInfos.keySet().iterator(); iter.hasNext();)
        {
            Integer key= iter.next();
            int[] endValues = baseToBaselineInfos.get(key);
            int inputStartLine = endValues[0];
            int repeatCount = endValues[1];
        	
        	lineBuffer.append(inputStartLine);
            lineBuffer.append("#" + baseClassFileId);
            
            if (repeatCount > 1)
            	lineBuffer.append("," + repeatCount);
            
            lineBuffer.append(":" + inputStartLine + "\n");
            lineInfos.put(new Integer(inputStartLine), lineBuffer.toString());
            lineBuffer.delete(0, lineBuffer.length());        	
        }
        
        
        for (Iterator<Integer> iter = lineInfos.keySet().iterator(); iter.hasNext();)
        {
            Integer inputStartline= iter.next();
            String lineInfo = lineInfos.get(inputStartline);
            
            out.append(lineInfo);
        }
        
        // print EndStratumSection
        out.append("*E");
        
        return out.toString().getBytes();
    }
    
    
}
