/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.version;

import org.apache.jackrabbit.core.*;
import org.apache.jackrabbit.core.nodetype.NodeTypeImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.util.uuid.UUID;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.OnParentVersionAction;
import java.util.ArrayList;
import java.util.List;

/**
 * The InternalFrozenNode class presents the frozen node that was generated
 * during a {@link javax.jcr.Node#checkin()}. It holds the set of frozen
 * properties, the frozen child nodes and the frozen version history
 * references of the original node.
 */
public class InternalFrozenNode extends InternalFreeze {

    private static final boolean FREEZEMODE_CLONE = true;

    /**
     * the underlaying persistance node
     */
    private PersistentNode node;

    /**
     * the list of frozen child nodes
     */
    private InternalFreeze[] frozenChildNodes;

    /**
     * the list of frozen properties
     */
    private PersistentProperty[] frozenProperties;

    /**
     * the frozen uuid of the original node
     */
    private String frozenUUID = null;

    /**
     * the frozen primary type of the orginal node
     */
    private QName frozenPrimaryType = null;

    /**
     * the frozen list of mixin types of the original node
     */
    private QName[] frozenMixinTypes = null;

    /**
     * uuid of this node
     */
    private String uuid;

    /**
     * Creates a new frozen node based on the given persistance node.
     *
     * @param node
     * @throws RepositoryException
     */
    protected InternalFrozenNode(InternalFreeze parent, PersistentNode node) throws RepositoryException {
        super(parent);
        this.node = node;

        // init the frozen properties
        PersistentProperty[] props = node.getProperties();
        List propList = new ArrayList();

        for (int i = 0; i < props.length; i++) {
            PersistentProperty prop = props[i];
            if (FREEZEMODE_CLONE) {
                if (prop.getName().equals(ItemImpl.PROPNAME_PRIMARYTYPE)) {
                    frozenPrimaryType = (QName) node.getPropertyValue(prop.getName()).internalValue();
                } else if (prop.getName().equals(ItemImpl.PROPNAME_UUID)) {
                    frozenUUID = node.getPropertyValue(prop.getName()).toString();
                } else if (prop.getName().equals(ItemImpl.PROPNAME_MIXINTYPES)) {
                    InternalValue[] values = node.getPropertyValues(prop.getName());
                    if (values == null) {
                        frozenMixinTypes = new QName[0];
                    } else {
                        frozenMixinTypes = new QName[values.length];
                        for (int j = 0; j < values.length; j++) {
                            frozenMixinTypes[j] = (QName) values[j].internalValue();
                        }
                    }
                } else {
                    propList.add(prop);
                }

            } else {
                if (prop.getName().equals(VersionManager.PROPNAME_FROZEN_UUID)) {
                    // special property
                    frozenUUID = node.getPropertyValue(VersionManager.PROPNAME_FROZEN_UUID).internalValue().toString();
                } else if (prop.getName().equals(VersionManager.PROPNAME_FROZEN_PRIMARY_TYPE)) {
                    // special property
                    frozenPrimaryType = (QName) node.getPropertyValue(VersionManager.PROPNAME_FROZEN_PRIMARY_TYPE).internalValue();
                } else if (prop.getName().equals(VersionManager.PROPNAME_FROZEN_MIXIN_TYPES)) {
                    // special property
                    InternalValue[] values = node.getPropertyValues(VersionManager.PROPNAME_FROZEN_MIXIN_TYPES);
                    if (values == null) {
                        frozenMixinTypes = new QName[0];
                    } else {
                        frozenMixinTypes = new QName[values.length];
                        for (int j = 0; j < values.length; j++) {
                            frozenMixinTypes[j] = (QName) values[j].internalValue();
                        }
                    }
                } else if (prop.getName().equals(ItemImpl.PROPNAME_PRIMARYTYPE)) {
                    // ignore
                } else if (prop.getName().equals(ItemImpl.PROPNAME_UUID)) {
                    // ignore
                } else {
                    propList.add(prop);
                }
            }
        }
        frozenProperties = (PersistentProperty[]) propList.toArray(new PersistentProperty[propList.size()]);

        // do some checks
        if (frozenMixinTypes == null) {
            frozenMixinTypes = new QName[0];
        }
        if (frozenPrimaryType == null) {
            throw new RepositoryException("Illegal frozen node. Must have 'frozenPrimaryType'");
        }
        // init the frozen child nodes
        PersistentNode[] childNodes = node.getChildNodes();
        frozenChildNodes = new InternalFreeze[childNodes.length];
        for (int i = 0; i < childNodes.length; i++) {
            if (childNodes[i].hasProperty(VersionManager.PROPNAME_FROZEN_PRIMARY_TYPE)) {
                frozenChildNodes[i] = new InternalFrozenNode(this, childNodes[i]);
            } else if (childNodes[i].hasProperty(VersionManager.PROPNAME_VERSION_HISTORY)) {
                frozenChildNodes[i] = new InternalFrozenVersionHistory(this, childNodes[i]);
            } else {
                // unkown ?
            }
        }

    }

    public String getInternalUUID() {
        return node.getUUID();
    }

    /**
     * Returns the name of this frozen node
     *
     * @return
     */
    public QName getName() {
        return node.getName();
    }

    /**
     * Returns the UUID of this frozen node
     *
     * @return
     */
    public String getUUID() {
        return node.getUUID();
    }

    /**
     * Returns the list of frozen child nodes
     *
     * @return
     */
    public InternalFreeze[] getFrozenChildNodes() {
        return frozenChildNodes;
    }

    /**
     * Returns the list of frozen properties
     *
     * @return
     */
    public PersistentProperty[] getFrozenProperties() {
        return frozenProperties;
    }

    /**
     * Returns the frozen UUID
     *
     * @return
     */
    public String getFrozenUUID() {
        return frozenUUID;
    }

    /**
     * Returns the frozen primary type
     *
     * @return
     */
    public QName getFrozenPrimaryType() {
        return frozenPrimaryType;
    }

    /**
     * Returns the list of the frozen mixin types
     *
     * @return
     */
    public QName[] getFrozenMixinTypes() {
        return frozenMixinTypes;
    }

    /**
     * Checks-in a <code>src</code> node. It creates a new child node of
     * <code>parent</code> with the given <code>name</code> and adds the
     * source nodes properties according to their OPV value to the
     * list of frozen properties. It creates frozen child nodes for each child
     * node of <code>src</code> according to its OPV value.
     *
     * @param parent
     * @param name
     * @param src
     * @return
     * @throws RepositoryException
     */
    protected static PersistentNode checkin(PersistentNode parent, QName name, NodeImpl src, boolean initOnly, boolean forceCopy)
            throws RepositoryException {

        PersistentNode node;
        if (FREEZEMODE_CLONE) {
            // identiycopy
            // create new node
            NodeType[] mixins = src.getMixinNodeTypes();
            QName[] mixinNames = new QName[mixins.length];
            for (int i = 0; i < mixins.length; i++) {
                mixinNames[i] = ((NodeTypeImpl) mixins[i]).getQName();
            }
            node = parent.addNode(name, ((NodeTypeImpl) src.getPrimaryNodeType()).getQName());
            node.setMixinNodeTypes(mixinNames);

        } else {
            // emulated
            // create new node
            node = parent.addNode(name, NodeTypeRegistry.NT_UNSTRUCTURED);

            // initialize the internal properties
            if (src.isNodeType(NodeTypeRegistry.MIX_REFERENCEABLE)) {
                node.setPropertyValue(VersionManager.PROPNAME_FROZEN_UUID, InternalValue.create(src.getUUID()));
            }

            node.setPropertyValue(VersionManager.PROPNAME_FROZEN_PRIMARY_TYPE,
                    InternalValue.create(((NodeTypeImpl) src.getPrimaryNodeType()).getQName()));

            if (src.hasProperty(NodeImpl.PROPNAME_MIXINTYPES)) {
                NodeType[] mixins = src.getMixinNodeTypes();
                InternalValue[] ivalues = new InternalValue[mixins.length];
                for (int i = 0; i < mixins.length; i++) {
                    ivalues[i] = InternalValue.create(((NodeTypeImpl) mixins[i]).getQName());
                }
                node.setPropertyValues(VersionManager.PROPNAME_FROZEN_MIXIN_TYPES, PropertyType.NAME, ivalues);
            }
        }

        if (!initOnly) {
            // add the properties
            PropertyIterator piter = src.getProperties();
            while (piter.hasNext()) {
                PropertyImpl prop = (PropertyImpl) piter.nextProperty();
                int opv = forceCopy ? OnParentVersionAction.COPY : prop.getDefinition().getOnParentVersion();
                switch (opv) {
                    case OnParentVersionAction.ABORT:
                        parent.reload();
                        throw new RepositoryException("Checkin aborted due to OPV in " + prop.safeGetJCRPath());
                    case OnParentVersionAction.COMPUTE:
                    case OnParentVersionAction.IGNORE:
                    case OnParentVersionAction.INITIALIZE:
                        break;
                    case OnParentVersionAction.VERSION:
                    case OnParentVersionAction.COPY:
                        node.copyFrom(prop);
                        break;
                }
            }


            // add the frozen children and vistories
            NodeIterator niter = src.getNodes();
            while (niter.hasNext()) {
                NodeImpl child = (NodeImpl) niter.nextNode();
                int opv = forceCopy ? OnParentVersionAction.COPY : child.getDefinition().getOnParentVersion();
                switch (opv) {
                    case OnParentVersionAction.ABORT:
                        throw new RepositoryException("Checkin aborted due to OPV in " + child.safeGetJCRPath());
                    case OnParentVersionAction.COMPUTE:
                    case OnParentVersionAction.IGNORE:
                    case OnParentVersionAction.INITIALIZE:
                        break;
                    case OnParentVersionAction.VERSION:
                        if (child.isNodeType(NodeTypeRegistry.MIX_VERSIONABLE)) {
                            // create frozen versionable child
                            PersistentNode newChild = node.addNode(child.getQName(), NodeTypeRegistry.NT_FROZEN_VERSIONABLE_CHILD);
                            newChild.setPropertyValue(VersionManager.PROPNAME_VERSION_HISTORY,
                                    InternalValue.create(UUID.fromString(child.getVersionHistory().getUUID())));
                            newChild.setPropertyValue(VersionManager.PROPNAME_BASE_VERSION,
                                    InternalValue.create(UUID.fromString(child.getBaseVersion().getUUID())));
                            break;
                        }
                        // else copy
                    case OnParentVersionAction.COPY:
                        checkin(node, child.getQName(), child, false, true);
                        break;
                }
            }
        }
        parent.store();
        return node;
    }

}