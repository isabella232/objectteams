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
 * $Id: ClassAttributeReader.java 23492 2010-02-05 22:57:56Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.tests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;

import de.fub.bytecode.Repository;
import de.fub.bytecode.classfile.Attribute;
import de.fub.bytecode.classfile.ClassParser;
import de.fub.bytecode.classfile.ConstantPool;
import de.fub.bytecode.classfile.ConstantUtf8;
import de.fub.bytecode.classfile.JavaClass;
import de.fub.bytecode.classfile.Method;
import de.fub.bytecode.classfile.Unknown;

/**
 * @author ike
 * 
 * Read class file(s) and display its Attributes. Copied and modified from
 * otlistclass.
 * 
 * $Id: ClassAttributeReader.java 23492 2010-02-05 22:57:56Z stephan $
 */
public class ClassAttributeReader
{

    static boolean printCompilerAttributes = false;

    private JavaClass _javaclass;

    private Hashtable _classAttributes  = new Hashtable();
    private Hashtable _methodAttributes = new Hashtable();
    private String _error;
    
    public ClassAttributeReader(IPath classFilePath)
    {
        if ((_javaclass = Repository.lookupClass(classFilePath.toOSString())) == null)
            try
            {
                _javaclass = new ClassParser(classFilePath.toOSString()).parse(); // May throw
                readAndStoreAttributes();
            }
            catch (ClassFormatError e)
            {
                _error = e.getMessage();
                e.printStackTrace();
            }
            catch (IOException e)
            {
                _error = e.getMessage();
                e.printStackTrace();
            }
    }
    
    public String getError()
    {
        return _error;
    }

    public boolean isSDEAvailable()
    {
        return _classAttributes.containsKey("SourceDebugExtension");
    }

    public char[] getSDE()
    {
        String[] sdeAsString = (String[]) _classAttributes.get("SourceDebugExtension");
        if (sdeAsString != null && sdeAsString[0] != null)
        {
            return sdeAsString[0].toCharArray();
        }
        return null;
    }

    
    public boolean isClassAttributeAvailable(String attributeName)
    {
        return _classAttributes.containsKey(attributeName);
    }
    
    public String[] getClassAttributeValues(String attributeName)
    {
        if (isClassAttributeAvailable(attributeName))
            return (String[])_classAttributes.get(attributeName);
        return null;
    }
    
    public void readAndStoreAttributes()
    {
        Attribute[] attributesAsArray = _javaclass.getAttributes();
        ConstantPool cp = _javaclass.getConstantPool();
        String class_name = _javaclass.getClassName();
        Vector values;
        
        for (int k = 0; k < attributesAsArray.length; k++)
        {
            Attribute actAttr = attributesAsArray[k];
            Unknown attr = isOTAttribute(actAttr);

            if (attr != null)
            { //this is a callin attribute

                String attrName = attr.getName();
                byte[] indizes = attr.getBytes();
                int count = combineTwoBytes(indizes, 0);
                int numberOfEntries = 0;
                String[] names;
                values = new Vector();

                if (attrName.equals("OTClassFlags"))
                {
                    int classFlags = combineTwoBytes(indizes, 0);
                    String flagsString = "";
                    if ((classFlags & 1) != 0)
                        flagsString += "team ";
                    if ((classFlags & 2) != 0)
                        flagsString += "role";
                    if ((classFlags & 4) != 0)
                        flagsString += "role-local"; // relevant for the
                                                     // compiler only.
                    values.add(new String[]{flagsString});

                }
                else if (attrName.equals("CallinRoleBaseBindings"))
                {
                    numberOfEntries = 2;
                    int i = 2;
                    while (i <= 2 * count * numberOfEntries)
                    {
                        names = new String[numberOfEntries];
                        i = scanStrings(names, indizes, i, cp);
                        String role_name = names[0];
                        String base_name = names[1];
                        values.add(new String[] { role_name, base_name });
                    }
                }
                else if (attrName.equals("CallinMethodMappings"))
                {
                    numberOfEntries = 5;
                    int i = 2;
                    for (int n = 0; n < count; n++)
                    {
                        names = new String[numberOfEntries];
                        i = scanStrings(names, indizes, i, cp);
                        String wrapper_name = null;
                        String wrapper_signature = null;
                        int index = 0;
                        String binding_label = names[index++];
                        String role_method_name = names[index++];
                        String role_method_signature = names[index++];
                        String real_role_return = names[index++];
                        String binding_modifier = names[index++];

                        int base_len = combineTwoBytes(indizes, i);
                        i += 2;
                        names = new String[4];
                        for (int n_base = 0; n_base < base_len; n_base++)
                        {
                            i = scanStrings(names, indizes, i, cp);
                            String base_method_name = names[0];
                            String base_method_signature = names[1];
                            wrapper_name = names[2];
                            wrapper_signature = names[3];

                            values.add(new String[] { binding_label, role_method_name,
                                                        role_method_signature, real_role_return,
                                                        binding_modifier, base_method_name,
                                                        base_method_signature, wrapper_name,
                                                        wrapper_signature});
                        }
                    }
                }
                else if (attrName.equals("CalloutBoundFields"))
                {
                    numberOfEntries = 2;
                    int i = 2;
                    for (int n = 0; n < count; n++)
                    {
                        names = new String[numberOfEntries];
                        i = scanStrings(names, indizes, i, cp);
                        String fieldName = names[0];
                        String fieldType = names[1];
                        int flags = combineTwoBytes(indizes, i);
                        String flagString = (flags & 1) == 1 ? "set " : "get ";
                        if ((flags & 2) != 0)
                            flagString = flagString + "static ";
                        i += 2;
                        values.add(new String[]{flagString, fieldType, fieldName});
                    }
                }
                else if (attrName.equals("InaccessibleBaseMethods"))
                {
                    numberOfEntries = 3;
                    int i = 2;
                    while (i <= 2 * count * numberOfEntries)
                    {
                        names = new String[numberOfEntries];
                        i = scanStrings(names, indizes, i, cp);
                        String base_class = names[0];
                        String base_method = names[1];
                        String base_signature = names[2];
                        values.add(new String[]{base_class, base_method, base_signature});
                    }
                }
                else if (attrName.equals("ReferencedTeams"))
                {
                    numberOfEntries = 1;
                    int i = 2;
                    while (i <= 2 * count * numberOfEntries)
                    {
                        names = new String[numberOfEntries];
                        i = scanStrings(names, indizes, i, cp);
                        String referenced_team = names[0];
                        values.add(new String[]{referenced_team});
                    }
                    
                }
                else if (attrName.equals("BaseClassTags"))
                {
                    numberOfEntries = 2;
                    String baseClass = "";
                    int tag = 0;
                    HashMap tagMap = new HashMap();

                    int i = 2;
                    while (i <= 2 * count * numberOfEntries)
                    {
                        for (int j = 0; j < numberOfEntries; j++)
                        {
                            int nextIndex = combineTwoBytes(indizes, i);
                            if (j == 0)
                            {
                                ConstantUtf8 cons = (ConstantUtf8) cp
                                        .getConstant(nextIndex);
                                baseClass = cons.getBytes();
                            }
                            else if (j == 1)
                            {
                                tag = nextIndex;
                            }
                            i += 2;
                        }
                        tagMap.put(baseClass, new Integer(tag));

                        values.add(new String[]{class_name, baseClass, String.valueOf(tag)});
                    }
                }
                else if (attrName.equals("PlayedBy"))
                {
                    names = new String[1];
                    scanStrings(names, indizes, 0, cp);
                    String base_class_name = names[0];
                    values.add(new String[]{base_class_name});

                }
                else if (attrName.equals("OTCompilerVersion"))
                {
                    int encodedVersion = combineTwoBytes(indizes, 0);
                    int major = encodedVersion >>> 9;
                    int minor = (encodedVersion >>> 5) & 0xF;
                    int revision = encodedVersion & 0x1F;
                    values.add(new String[]{String.valueOf(major), String.valueOf(minor), String.valueOf(revision)});

                }
                else if (attrName.equals("CallinPrecedence"))
                {
                    List precedenceList = new LinkedList();
                    numberOfEntries = 1;
                    int i = 2;
                    while (i <= 2 * count * numberOfEntries)
                    {
                        names = new String[numberOfEntries];
                        i = scanStrings(names, indizes, i, cp);
                        String binding_label = names[0];
                        precedenceList.add(binding_label);
                    }
                    values.add(new String[]{precedenceList.toString()});
                } // ==== Following: attributes relevant for the compiler only:
                  // ====
                else if (attrName.equals("RoleFiles"))
                {
                    numberOfEntries = 1;
                    int i = 2;
                    while (i <= 2 * count * numberOfEntries)
                    {
                        names = new String[numberOfEntries];
                        i = scanStrings(names, indizes, i, cp);
                        String roleFile = names[0];
                        values.add(new String[]{roleFile});
                    }
                }
                else if (attrName.equals("CalloutMethodMappings"))
                {
                    numberOfEntries = 2;
                    int i = 2;
                    for (int n = 0; n < count; n++)
                    {
                        names = new String[numberOfEntries];
                        i = scanStrings(names, indizes, i, cp);
                        int index = 0;
                        String role_method_name = names[index++];
                        String role_method_signature = names[index++];

                        values.add(new String[]{role_method_name, role_method_signature});
                    }
                }
                else if (attrName.equals("SourceDebugExtension"))
                {
                    String smap = new String();
                    for (int idx = 0; idx < indizes.length; idx++)
                    {
                        smap += String.valueOf((char) indizes[idx]);
                    }
                    values.add(new String[]{smap});
                }
                
                _classAttributes.put(attrName, values);
            }

        }

        Method[] possibleRoleMethods = _javaclass.getMethods();

        for (int i = 0; i < possibleRoleMethods.length; i++)
        {
            Method meth = possibleRoleMethods[i];
            Attribute[] attrsMethod = meth.getAttributes();
            String method_name = meth.getName();
            //TODO(ike): handle and store attributes of methodes also
//            scanMethodOTAttributes(attrsMethod, class_name, method_name, cp);
        }
    }

    Unknown isOTAttribute(Attribute attr)
    {
        if (attr instanceof Unknown)
        {
            Unknown unknown = (Unknown) attr;
            String attrName = unknown.getName();
            if (attrName.equals("CallinRoleBaseBindings")
                    || attrName.equals("CallinMethodMappings")
                    || attrName.equals("CallinParamMappings")
                    || attrName.equals("CallinFlags")
                    || attrName.equals("CalloutBoundFields")
                    || attrName.equals("WrappedRoleSignature")
                    || attrName.equals("WrappedBaseSignature")
                    || attrName.equals("InaccessibleBaseMethods")
                    || attrName.equals("ReferencedTeams")
                    || attrName.equals("BaseClassTags")
                    || attrName.equals("PlayedBy")
                    || attrName.equals("Modifiers")
                    || attrName.equals("OTCompilerVersion")
                    || attrName.equals("OTClassFlags")
                    || attrName.equals("CallinPrecedence")
                    || attrName.equals("SourceDebugExtension"))
            {
                return unknown;
            }
            if (printCompilerAttributes)
            {
                if (attrName.equals("RoleFiles")
                        || attrName.equals("CalloutMethodMappings")
                        || attrName.equals("Modifiers")
                        || attrName.equals("CopyInheritanceSrc"))
                {
                    return unknown;
                }
            }
        }
        return null;
    }

    /**
     * combines two int's representing the higher and the lower part of a two
     * byte number.
     * 
     * @param first
     *            the first (higer?) byte
     * @param second
     *            the second (lower?) byte
     * @return the combined number
     */
    int combineTwoBytes(byte[] indizes, int start)
    {
        int first = indizes[start];
        int second = indizes[start + 1];
        int twoBytes = 0;

        twoBytes = twoBytes | (first & 0xff);
        twoBytes = twoBytes << 8;
        twoBytes = twoBytes | (second & 0xff);
        return twoBytes;
    }

    /**
     * Read some strings from a byte array.
     * 
     * @param entries
     *            Result array to be provided by caller.
     * @param indizes
     *            buffer of read bytes to be provided by caller, consists if
     *            indizes into the constant pool
     * @param i
     *            current index into indizes
     * @param cp
     *            the pool.
     * @result updated value of <tt>i</tt>.
     */
    int scanStrings(String[] entries, byte[] indizes, int i, ConstantPool cp)
    {
        for (int j = 0; j < entries.length; j++)
        {
            int nextIndex = combineTwoBytes(indizes, i);
            ConstantUtf8 cons = (ConstantUtf8) cp.getConstant(nextIndex);
            String content = cons.getBytes();
            entries[j] = content;
            i += 2;
        }
        return i;
    }

    void scanMethodOTAttributes(Attribute[] attributes, String class_name,
            String method_name, ConstantPool cp)
    {
        Vector methodAttributesValues = new Vector();
        boolean ot_attribute_found = false;
        for (int k = 0; k < attributes.length; k++)
        {
            Attribute actAttr = attributes[k];
            Unknown attr = isOTAttribute(actAttr);

            if (attr != null)
            { //this is an OT attribute
                if (ot_attribute_found == false)
                {
                    System.out.println();
                    System.out.println("--- in method '" + method_name
                            + "': ---");
                    ot_attribute_found = true;
                }
                String attrName = attr.getName();
                if (attrName.equals("CallinParamMappings"))
                {
                    System.out.println("CallinParamMappings:");
                    byte[] indizes = attr.getBytes();
                    int[] positions = null;
                    if (indizes == null)
                        throw new RuntimeException("Unexpected null attr");
                    int count = combineTwoBytes(indizes, 0);
                    positions = new int[count];
                    int p = 2;
                    for (int i = 0; i < count; i++, p += 2)
                    {
                        positions[i] = combineTwoBytes(indizes, p);
                        System.out.println("\t" + i + " <- " + positions[i]);
                    }

                }
                else if (attrName.equals("WrappedRoleSignature"))
                {
                    System.out.println("WrappedRoleSignature:");
                    byte[] indizes = attr.getBytes();
                    String[] names = new String[1];
                    scanStrings(names, indizes, 0, cp);
                    String role_signature = names[0];
                    System.out.println("\t" + role_signature);
                }
                else if (attrName.equals("WrappedBaseSignature"))
                {
                    System.out.println("WrappedBaseSignature:");
                    byte[] indizes = attr.getBytes();
                    String[] names = new String[1];
                    scanStrings(names, indizes, 0, cp);
                    String base_signature = names[0];
                    System.out.println("\t" + base_signature);
                }
                else if (attrName.equals("CallinFlags"))
                {
                    System.out.print("CallinFlags: ");
                    byte[] bytes = attr.getBytes();
                    int flags = combineTwoBytes(bytes, 0);
                    if ((flags & 1) != 0)
                        System.out.print("OVERRIDE ");
                    if ((flags & 2) != 0)
                        System.out.print("WRAPPER ");
                    if ((flags & 4) != 0)
                        System.out.print("REPLACE ");
                    System.out.println();
                } // ==== Following: attributes relevant for the compiler only:
                  // ====
                else if (attrName.equals("Modifiers"))
                {
                    System.out.print("Modifiers: 0x");
                    byte[] bytes = attr.getBytes();
                    int flags = combineTwoBytes(bytes, 0);
                    System.out.println(Integer.toHexString(flags));
                }
                else if (attrName.equals("CopyInheritanceSrc"))
                {
                    System.out.println("CopyInheritanceSrc:");
                    byte[] indizes = attr.getBytes();
                    String[] names = new String[1];
                    scanStrings(names, indizes, 0, cp);
                    String src_name = names[0];
                    System.out.println("\t" + src_name);
                }

            }
        }
    }

}
