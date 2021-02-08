/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.bdd.model;

import java.util.Deque;
import java.util.LinkedList;

import org.vividus.bdd.Status;

public class Node
{
    private final NodeType type;
    private final Deque<Node> children = new LinkedList<>();
    private Status status;
    private boolean hasChildrens;

    private Node parent;

    public Node(NodeType type)
    {
        this.type = type;
    }

    public Status getStatus()
    {
        return status;
    }

    public Node withStatus(Status status)
    {
        this.status = status;
        return this;
    }

    public Node getParent()
    {
        return parent;
    }

    public void setParent(Node parent)
    {
        this.parent = parent;
    }

    public Deque<Node> getChildren()
    {
        return children;
    }

    public Node addChild(Node child)
    {
        this.children.add(child);
        child.setParent(this);
        return this;
    }

    public NodeType getType()
    {
        return type;
    }

    public void setHasChildrens(boolean hasChildrens)
    {
        this.hasChildrens = hasChildrens;
    }

    public boolean isHasChildrens()
    {
        return hasChildrens;
    }
}
