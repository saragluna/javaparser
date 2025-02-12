/*
 * Copyright (C) 2015-2016 Federico Tomassetti
 * Copyright (C) 2017-2019 The JavaParser Team.
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

package com.github.javaparser.symbolsolver.reflectionmodel;

import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReflectionParameterDeclarationTest {

    @Test
    void testGetSignature() {
        TypeSolver typeResolver = new ReflectionTypeSolver();

        ResolvedClassDeclaration object = new ReflectionClassDeclaration(Object.class, typeResolver);
        ResolvedInterfaceDeclaration list = new ReflectionInterfaceDeclaration(List.class, typeResolver);

        ResolvedMethodDeclaration hashCode = object.getAllMethods().stream().filter(m -> m.getName().equals("hashCode")).findFirst().get().getDeclaration();
        ResolvedMethodDeclaration equals = object.getAllMethods().stream().filter(m -> m.getName().equals("equals")).findFirst().get().getDeclaration();
        ResolvedMethodDeclaration containsAll = list.getAllMethods().stream().filter(m -> m.getName().equals("containsAll")).findFirst().get().getDeclaration();
        ResolvedMethodDeclaration subList = list.getAllMethods().stream().filter(m -> m.getName().equals("subList")).findFirst().get().getDeclaration();

        assertEquals("hashCode()", hashCode.getSignature());
        assertEquals("equals(java.lang.Object)", equals.getSignature());
        assertEquals("containsAll(java.util.Collection<? extends java.lang.Object>)", containsAll.getSignature());
        assertEquals("subList(int, int)", subList.getSignature());
    }

    @Test
    void testGetGenericReturnType() {
        TypeSolver typeResolver = new ReflectionTypeSolver();

        ResolvedInterfaceDeclaration map = new ReflectionInterfaceDeclaration(Map.class, typeResolver);

        ResolvedMethodDeclaration put = map.getAllMethods().stream().filter(m -> m.getName().equals("put")).findFirst().get().getDeclaration();
        assertEquals(true, put.getReturnType().isTypeVariable());
        assertEquals(true, put.getReturnType().asTypeParameter().declaredOnType());
        assertEquals("java.util.Map.V", put.getReturnType().asTypeParameter().getQualifiedName());
    }

    @Test
    void testGetGenericParameters() {
        TypeSolver typeResolver = new ReflectionTypeSolver();

        ResolvedInterfaceDeclaration map = new ReflectionInterfaceDeclaration(Map.class, typeResolver);

        ResolvedMethodDeclaration put = map.getAllMethods().stream().filter(m -> m.getName().equals("put")).findFirst().get().getDeclaration();
        assertEquals(true, put.getParam(0).getType().isTypeVariable());
        assertEquals(true, put.getParam(0).getType().asTypeParameter().declaredOnType());
        assertEquals("java.util.Map.K", put.getParam(0).getType().asTypeParameter().getQualifiedName());

        assertEquals(true, put.getParam(1).getType().isTypeVariable());
        assertEquals(true, put.getParam(1).getType().asTypeParameter().declaredOnType());
        assertEquals("java.util.Map.V", put.getParam(1).getType().asTypeParameter().getQualifiedName());
    }
}
