/**
 *  Copyright 2012-2014 Gunnar Morling (http://www.gunnarmorling.de/)
 *  and/or other contributors as indicated by the @authors tag. See the
 *  copyright.txt file in the distribution for a full listing of all
 *  contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mapstruct.ap.test.collection.adder;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.ap.test.collection.adder.source.Source;
import org.mapstruct.ap.test.collection.adder.source.SourceTeeth;
import org.mapstruct.ap.test.collection.adder.target.AdderUsageObserver;
import org.mapstruct.ap.test.collection.adder.target.IndoorPet;
import org.mapstruct.ap.test.collection.adder.target.OutdoorPet;
import org.mapstruct.ap.test.collection.adder.target.Pet;
import org.mapstruct.ap.test.collection.adder.target.Target;
import org.mapstruct.ap.test.collection.adder.target.TargetDali;
import org.mapstruct.ap.test.collection.adder.target.TargetHuman;
import org.mapstruct.ap.test.collection.adder.target.TargetOnlyGetter;
import org.mapstruct.ap.test.collection.adder.target.TargetViaTargetType;
import org.mapstruct.ap.test.collection.adder.target.TargetWithoutSetter;
import org.mapstruct.ap.testutil.WithClasses;
import org.mapstruct.ap.testutil.runner.AnnotationProcessorTestRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Sjaak Derksen
 */
@WithClasses({
    Source.class,
    SourceTeeth.class,
    Target.class,
    TargetDali.class,
    TargetHuman.class,
    TargetOnlyGetter.class,
    TargetViaTargetType.class,
    TargetWithoutSetter.class,
    SourceTargetMapper.class,
    SourceTargetMapperStrategyDefault.class,
    SourceTargetMapperStrategySetterPreferred.class,
    PetMapper.class,
    TeethMapper.class,
    AdderUsageObserver.class,
    Pet.class,
    IndoorPet.class,
    OutdoorPet.class,
    DogException.class,
    CatException.class
})
@RunWith(AnnotationProcessorTestRunner.class)
public class AdderTest {

    @Test
    public void testAdd() throws DogException {
        AdderUsageObserver.setUsed( false );

        Source source = new Source();
        source.setPets( Arrays.asList( "mouse" ) );

        Target target = SourceTargetMapper.INSTANCE.toTarget( source );
        assertThat( target ).isNotNull();
        assertThat( target.getPets().size() ).isEqualTo( 1 );
        assertThat( target.getPets().get( 0 ) ).isEqualTo( 2L );
        assertTrue( AdderUsageObserver.isUsed() );
    }

    @Test(expected = DogException.class)
    public void testAddWithExceptionInThrowsClause() throws DogException {
        AdderUsageObserver.setUsed( false );

        Source source = new Source();
        source.setPets( Arrays.asList( "dog" ) );

        SourceTargetMapper.INSTANCE.toTarget( source );
    }

    @Test(expected = RuntimeException.class)
    public void testAddWithExceptionNotInThrowsClause() throws DogException {
        AdderUsageObserver.setUsed( false );

        Source source = new Source();
        source.setPets( Arrays.asList( "cat" ) );

        SourceTargetMapper.INSTANCE.toTarget( source );
    }

    @Test
    public void testAddwithExistingTarget() throws DogException {
        AdderUsageObserver.setUsed( false );

        Source source = new Source();
        source.setPets( Arrays.asList( "mouse" ) );

        Target target = new Target();
        target.setPets( new ArrayList<Long>( Arrays.asList( 1L ) ) );

        SourceTargetMapper.INSTANCE.toExistingTarget( source, target );
        assertThat( target ).isNotNull();
        assertThat( target.getPets().size() ).isEqualTo( 2 );
        assertThat( target.getPets().get( 0 ) ).isEqualTo( 1L );
        assertThat( target.getPets().get( 1 ) ).isEqualTo( 2L );
        assertTrue( AdderUsageObserver.isUsed() );
    }

    @Test
    public void testShouldUseDefaultStrategy() throws DogException {
        AdderUsageObserver.setUsed( false );

        Source source = new Source();
        source.setPets( Arrays.asList( "mouse" ) );

        Target target = SourceTargetMapperStrategyDefault.INSTANCE.shouldFallBackToAdder( source );
        assertThat( target ).isNotNull();
        assertThat( target.getPets().size() ).isEqualTo( 1 );
        assertThat( target.getPets().get( 0 ) ).isEqualTo( 2L );
        assertFalse( AdderUsageObserver.isUsed() );
    }

    @Test
    public void testShouldPreferSetterStrategyButThereIsNone() throws DogException {
        AdderUsageObserver.setUsed( false );

        Source source = new Source();
        source.setPets( Arrays.asList( "mouse" ) );

        TargetWithoutSetter target = SourceTargetMapperStrategySetterPreferred.INSTANCE.toTargetDontUseAdder( source );
        assertThat( target ).isNotNull();
        assertThat( target.getPets().size() ).isEqualTo( 1 );
        assertThat( target.getPets().get( 0 ) ).isEqualTo( 2L );
        assertTrue( AdderUsageObserver.isUsed() );
    }

    @Test
    public void testShouldPreferHumanSingular() {

        AdderUsageObserver.setUsed( false );

        SourceTeeth source = new SourceTeeth();
        source.setTeeth( Arrays.asList( "moler" ) );

        TargetHuman target = SourceTargetMapper.INSTANCE.toTargetHuman( source );
        assertThat( target ).isNotNull();
        assertThat( target.getTeeth().size() ).isEqualTo( 1 );
        assertThat( target.getTeeth().get( 0 ) ).isEqualTo( 3 );
        assertTrue( AdderUsageObserver.isUsed() );
    }

    @Test
    public void testshouldFallBackToDaliSingularInAbsenseOfHumanSingular() {
        AdderUsageObserver.setUsed( false );

        SourceTeeth source = new SourceTeeth();
        source.setTeeth( Arrays.asList( "moler" ) );

        TargetDali target = SourceTargetMapper.INSTANCE.toTargetDali( source );
        assertThat( target ).isNotNull();
        assertThat( target.getTeeth().size() ).isEqualTo( 1 );
        assertThat( target.getTeeth().get( 0 ) ).isEqualTo( 3 );
        assertTrue( AdderUsageObserver.isUsed() );
    }

    @Test
    public void testAddReverse() throws DogException {
        AdderUsageObserver.setUsed( false );

        Target source = new Target();
        source.setPets( Arrays.asList( 3L ) );

        Source target = SourceTargetMapper.INSTANCE.toSource( source );
        assertThat( target ).isNotNull();
        assertThat( target.getPets().size() ).isEqualTo( 1 );
        assertThat( target.getPets().get( 0 ) ).isEqualTo( "cat" );
    }

    @Test
    public void testAddOnlyGetter() throws DogException {
        AdderUsageObserver.setUsed( false );

        Source source = new Source();
        source.setPets( Arrays.asList( "mouse" ) );

        TargetOnlyGetter target = SourceTargetMapper.INSTANCE.toTargetOnlyGetter( source );
        assertThat( target ).isNotNull();
        assertThat( target.getPets().size() ).isEqualTo( 1 );
        assertThat( target.getPets().get( 0 ) ).isEqualTo( 2L );
        assertTrue( AdderUsageObserver.isUsed() );
    }

    @Test
    public void testAddViaTargetType() throws DogException {
        AdderUsageObserver.setUsed( false );

        Source source = new Source();
        source.setPets( Arrays.asList( "mouse" ) );

        TargetViaTargetType target = SourceTargetMapper.INSTANCE.toTargetViaTargetType( source );
        assertThat( target ).isNotNull();
        assertThat( target.getPets().size() ).isEqualTo( 1 );
        assertThat( target.getPets().get( 0 ) ).isNotNull();
        assertThat( target.getPets().get( 0 ).getValue() ).isEqualTo( 2L );
        assertTrue( AdderUsageObserver.isUsed() );
    }

}
