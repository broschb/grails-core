package grails.test

import grails.persistence.*

import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class MockDomainWithInheritanceTests extends GroovyTestCase {

    @Override
    protected void tearDown() {
        ApplicationHolder.application = null
    }

    void testMockDomainWithInheritance() {
        def test = new PersonTests()
        test.setUp()
        test.testLoadingPirateInstance()
        test.tearDown()
    }
}

class PersonTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        def aPerson = new Person(name: "Rob Fletcher")
        def aPirate = new Pirate(name: "Edward Teach", nickname: "Blackbeard")
        mockDomain(Person, [aPerson, aPirate])
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        ApplicationHolder.application = null
    }

    void testLoadingPersonInstance() {
        def person = Person.findByName("Rob Fletcher")
        assertTrue person instanceof Person
        assertFalse person instanceof Pirate
    }

    void testLoadingPirateInstance() {
        def person = Person.findByName("Edward Teach")
        assertTrue person instanceof Person
        assertTrue person instanceof Pirate
        assertEquals("Blackbeard", person.nickname)

        person = Pirate.findByName("Edward Teach")

        assertNotNull "should have found a pirate", person
        assertTrue person instanceof Person
        assertTrue person instanceof Pirate
        assertEquals("Blackbeard", person.nickname)

        assertNull "That's not a pirate!", Pirate.findByName("Rob Fletcher")
    }
}

@Entity
class Person {
    String name
}

@Entity
class Pirate extends Person {
    String nickname
}
