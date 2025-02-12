/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2021 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.generator.core.visitor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.generator.VisitorGenerator;
import com.github.javaparser.metamodel.BaseNodeMetaModel;
import com.github.javaparser.metamodel.CompilationUnitMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.utils.SeparatedItemStringBuilder;
import com.github.javaparser.utils.SourceRoot;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

/**
 * Generates JavaParser's CloneVisitor.
 */
public class CloneVisitorGenerator extends VisitorGenerator {
    public CloneVisitorGenerator(SourceRoot sourceRoot) {
        super(sourceRoot, "com.github.javaparser.ast.visitor", "CloneVisitor", "Visitable", "Object", true);
    }

    @Override
    protected void generateVisitMethodBody(BaseNodeMetaModel node, MethodDeclaration visitMethod, CompilationUnit compilationUnit) {
        visitMethod.getParameters().forEach(p -> p.setFinal(true));

        BlockStmt body = visitMethod.getBody().get();
        body.getStatements().clear();

        for (PropertyMetaModel field : node.getAllPropertyMetaModels()) {
            final String getter = field.getGetterMethodName() + "()";
            if (field.getNodeReference().isPresent()) {
                if (field.isOptional() && field.isNodeList()) {
                    body.addStatement(f("NodeList<%s> %s = cloneList(n.%s.orElse(null), arg);", field.getTypeNameGenerified(), field.getName(), getter));
                } else if (field.isNodeList()) {
                    body.addStatement(f("NodeList<%s> %s = cloneList(n.%s, arg);", field.getTypeNameGenerified(), field.getName(), getter));
                } else {
                    body.addStatement(f("%s %s = cloneNode(n.%s, arg);", field.getTypeNameGenerified(), field.getName(), getter));
                }
            }
        }

        SeparatedItemStringBuilder builder = new SeparatedItemStringBuilder(f("%s r = new %s(", node.getTypeNameGenerified(), node.getTypeNameGenerified()), ",", ");");
        builder.append("n.getTokenRange().orElse(null)");
        for (PropertyMetaModel field : node.getConstructorParameters()) {
            if (field.getName().equals("comment")) {
                continue;
            }
            if (field.getNodeReference().isPresent()) {
                builder.append(field.getName());
            } else {
                builder.append(f("n.%s()", field.getGetterMethodName()));
            }
        }

        body.addStatement(builder.toString());
        if(node instanceof CompilationUnitMetaModel) {
            body.addStatement("n.getStorage().ifPresent(s -> r.setStorage(s.getPath(), s.getEncoding()));");
        }
        body.addStatement("r.setComment(comment);");
        body.addStatement("n.getOrphanComments().stream().map(Comment::clone).forEach(r::addOrphanComment);");
        body.addStatement("copyData(n, r);");
        body.addStatement("return r;");
    }
}
