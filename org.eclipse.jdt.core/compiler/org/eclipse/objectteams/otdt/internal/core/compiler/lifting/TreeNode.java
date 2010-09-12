/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TreeNode.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import java.util.Vector;

import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;


/**
 * MIGRATION_STATE: complete
 *
 * @author mac
 *
 */
public class TreeNode
{
    /** if a return value is equal to ProblemNode, it encodes a hierarchy problem.*/
    public static final TreeNode ProblemNode = new TreeNode(null); // unique object

    private RoleModel   typeModel;
    private TreeNode[]  parents = null;
    private TreeNode[]  children;

    public TreeNode(RoleModel typeModel)
    {
        this.typeModel = typeModel;
    }

    // for ProblemNode setup:
    private void setParents(TreeNode r1, TreeNode r2) {
        this.parents = new TreeNode[]{ r1, r2 };
    }

    public RoleModel getTreeObject()
    {
        return this.typeModel;
    }

    public boolean isRoot()
    {
        return this.parents == null;
    }

    public boolean hasChildren()
    {
        if ((this.children == null) || (this.children.length == 0))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public TreeNode[] getChildren()
    {
        return this.children;
    }

    /**
     * add a child to this TreeNode and also link this as a parent of child.
     * @param child
     */
    public void add(TreeNode child)
    {
        if (hasChildren())
        {
            TreeNode[] newChildren = new TreeNode[this.children.length + 1];
            System.arraycopy(
                this.children, 0, newChildren, 0, this.children.length);
            newChildren[this.children.length]     = child;
            this.children                         = newChildren;
        }
        else
        {
            this.children        = new TreeNode[1];
            this.children[0]     = child;
        }

        child.setParent(this);
    }

    private void setParent(TreeNode parent)
    {
        int len = (this.parents == null) ? 1 : this.parents.length+1;
        TreeNode[] newParents = new TreeNode[len];
        if (this.parents != null)
            System.arraycopy(this.parents,0, newParents,0, len-1);
        this.parents = newParents;
        this.parents[len-1] = parent;
    }


    /**
     * Does this node have a bound parent not including self?
     * @param interfaceAllowed should regular interfaces be considered, too?
     * @return hasBoundParent
     */
    public boolean hasBoundParent(boolean interfaceAllowed) {
        if (this.parents == null)
            return false;
        for (int i=0; i<this.parents.length; i++) {
            TreeNode parent = this.parents[i];
            if (   parent.getTreeObject().isBound()
                && ( interfaceAllowed || !parent.getTreeObject().isRegularInterface()))
                return true;
            if (parent.hasBoundParent(interfaceAllowed))
            	return true;
        }
        return false;
    }
    /**
     * Retrieve the least specific bound parent node not including self
     * @param interfaceAllowed should regular interfaces be considered, too?
     * @return the bound parent or null
     */
    public TreeNode getBoundParent(boolean interfaceAllowed) {
        if (this.parents == null)
            return null;
        for (int i=0; i<this.parents.length; i++) {
            TreeNode parent = this.parents[i];
            // first look further up:
            TreeNode lessSpecificParent = parent.getBoundParent(interfaceAllowed);
            if (lessSpecificParent != null)
            	return lessSpecificParent;
            // if not found check parent itself:
            if (   parent.getTreeObject().isBound()
            	&& ( interfaceAllowed || !parent.getTreeObject().isRegularInterface()))
            	return parent;
        }
        return null;
    }


    /**
     * Get the top most bound parent including this.
     * If multiple candidates exist, return ProblemNode and set it up to
     * give a error message via toString().
     *
     * @param interfaceAllowed should regular interfaces be considered, too?
     */
    public TreeNode getTopmostBoundParent(boolean interfaceAllowed) {
        TreeNode found = null;
        if (this.parents != null) {
            for (int i=0; i<this.parents.length; i++) {
                TreeNode parent = this.parents[i];
                TreeNode current = parent.getTopmostBoundParent(interfaceAllowed);
                if (current != null) {
                    if (found == null) {
                        found = current;
                    } else if (found != current) {
                        ProblemNode.setParents(found, current);
                        return ProblemNode;
                    }
                }
            }
            if (found != null)
                return found;
        }
        if (   getTreeObject().isBound()
            && ( interfaceAllowed || !getTreeObject().isRegularInterface()))
            return this;
        return null;
    }

    @SuppressWarnings("nls")
	public String toString()
    {
        // assemble the message for ProblemNode: list of bound parent roles:
        if (this == ProblemNode)
            return new String(this.parents[0].getTreeObject().getBinding().sourceName())
                 + " and "
                 + new String(this.parents[1].getTreeObject().getBinding().sourceName());

        return toString(0);
    }

    @SuppressWarnings("nls")
	public String toString(int tab)
    {
        // regular printing:
        String str = tabString(tab) + this.typeModel.toString() + "\n";
        if (!hasChildren())
        {

            return str;
        }

        for (int i = 0; i < this.children.length; i++)
        {
            TreeNode treeNode = this.children[i];
            str += treeNode.toString(tab + 1);
        }

        return str;
    }

    @SuppressWarnings("nls")
	public static String tabString(int tab)
    {
        String s = "";
        for (int i = tab; i > 0; i--)
        {
            s = s + "  ";
        }

        return s;
    }

    public TreeNode[] elements()
    {
        Vector<TreeNode> treeNodes = new Vector<TreeNode>(0, 1);
        treeNodes.add(this);
        if (hasChildren())
        {
            for (int i = 0; i < this.children.length; i++)
            {
                TreeNode   childNode = this.children[i];
                TreeNode[] nodes = childNode.elements();
                for (int j = 0; j < nodes.length; j++)
                {
                    TreeNode node = nodes[j];
                    treeNodes.add(node);
                }
            }
        }

        return treeNodes.toArray(new TreeNode[treeNodes.size()]);
    }

    public TreeNode find(ReferenceBinding binding)
    {
        if (getTreeObject().getBinding() == binding)
        {

            return this;
        }
        else
        {
            if (hasChildren())
            {
                for (int i = 0; i < this.children.length; i++)
                {
                    TreeNode childNode = this.children[i];
                    TreeNode node = childNode.find(binding);
                    if (node != null)
                    {

                        return node;
                    }
                }
            }
        }

        return null;
    }
}
