package org.semanticweb.owlapitools.profiles.test;

import static org.junit.Assert.assertEquals;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;
import static org.semanticweb.owlapi.search.Searcher.find;

import java.net.URL;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.Searcher;
import org.semanticweb.owlapitools.profiles.OWL2DLProfile;
import org.semanticweb.owlapitools.profiles.OWL2ELProfile;
import org.semanticweb.owlapitools.profiles.OWL2Profile;
import org.semanticweb.owlapitools.profiles.OWL2QLProfile;
import org.semanticweb.owlapitools.profiles.OWL2RLProfile;
import org.semanticweb.owlapitools.profiles.OWLProfile;
import org.semanticweb.owlapitools.profiles.OWLProfileReport;

/** @author Matthew Horridge, The University of Manchester, Information Management
 *         Group, Date: 18-Aug-2009 */
@SuppressWarnings("javadoc")
public class ProfileValidationTestCase {
    private static final String TEST_NAMESPACE = "http://www.w3.org/2007/OWL/testOntology#";
    private static final IRI PROFILE_IDENTIFICATION_TEST_IRI = IRI(TEST_NAMESPACE
            + "ProfileIdentificationTest");
    private static final IRI SPECIES_IRI = IRI(TEST_NAMESPACE + "species");
    private static final IRI FULL_IRI = IRI(TEST_NAMESPACE + "FULL");
    private static final IRI DL_IRI = IRI(TEST_NAMESPACE + "DL");
    private static final IRI EL_IRI = IRI(TEST_NAMESPACE + "EL");
    private static final IRI QL_IRI = IRI(TEST_NAMESPACE + "QL");
    private static final IRI RL_IRI = IRI(TEST_NAMESPACE + "RL");
    private static final IRI RDF_XML_PREMISE_ONTOLOGY_IRI = IRI(TEST_NAMESPACE
            + "rdfXmlPremiseOntology");

    @Test
    public void testProfiles() throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        URL resourceURL = ProfileValidationTestCase.class
                .getResource("/all.rdf");
        IRI allTestURI = IRI.create(resourceURL);
        OWLOntology testCasesOntology = man
                .loadOntologyFromOntologyDocument(allTestURI);
        OWLDataFactory df = man.getOWLDataFactory();
        OWLClass profileIdentificationTestClass = Class(PROFILE_IDENTIFICATION_TEST_IRI);
        OWLNamedIndividual EL = df.getOWLNamedIndividual(EL_IRI);
        OWLNamedIndividual QL = df.getOWLNamedIndividual(QL_IRI);
        OWLNamedIndividual RL = df.getOWLNamedIndividual(RL_IRI);
        OWLObjectProperty speciesProperty = df
                .getOWLObjectProperty(SPECIES_IRI);
        OWLNamedIndividual FULL = df.getOWLNamedIndividual(FULL_IRI);
        OWLNamedIndividual DL = df.getOWLNamedIndividual(DL_IRI);
        OWLDataProperty rdfXMLPremiseOntologyProperty = df
                .getOWLDataProperty(RDF_XML_PREMISE_ONTOLOGY_IRI);
        for (OWLClassAssertionAxiom ax : testCasesOntology
                .getClassAssertionAxioms(profileIdentificationTestClass)) {
            OWLIndividual ind = ax.getIndividual();
            Searcher<OWLLiteral> vals = find(OWLLiteral.class)
                    .values(rdfXMLPremiseOntologyProperty).individual(ind)
                    .in(testCasesOntology);
            if (vals.size() != 1) {
                continue;
            }
            String ontologySerialisation = vals.iterator().next().getLiteral();
            OWLOntology ontology = man
                    .loadOntologyFromOntologyDocument(new StringDocumentSource(
                            ontologySerialisation));
            // FULL?
            Searcher<OWLIndividual> finder = find(OWLIndividual.class)
                    .values(speciesProperty).individual(ind)
                    .in(testCasesOntology);
            if (finder.contains(FULL)) {
                checkProfile(ontology, new OWL2Profile(), true);
            }
            Searcher<OWLIndividual> negativeFinder = find(OWLIndividual.class)
                    .negativeValues(speciesProperty).individual(ind)
                    .in(testCasesOntology);
            if (negativeFinder.contains(FULL)) {
                checkProfile(ontology, new OWL2Profile(), false);
            }
            // DL?
            if (finder.contains(DL)) {
                checkProfile(ontology, new OWL2DLProfile(), true);
            }
            if (negativeFinder.contains(DL)) {
                checkProfile(ontology, new OWL2DLProfile(), false);
            }
            // EL?
            if (finder.contains(EL)) {
                checkProfile(ontology, new OWL2ELProfile(), true);
            }
            if (negativeFinder.contains(EL)) {
                checkProfile(ontology, new OWL2ELProfile(), false);
            }
            // QL?
            if (finder.contains(QL)) {
                checkProfile(ontology, new OWL2QLProfile(), true);
            }
            if (negativeFinder.contains(QL)) {
                checkProfile(ontology, new OWL2QLProfile(), false);
            }
            // RL?
            if (finder.contains(RL)) {
                checkProfile(ontology, new OWL2RLProfile(), true);
            }
            if (negativeFinder.contains(RL)) {
                checkProfile(ontology, new OWL2RLProfile(), false);
            }
            man.removeOntology(ontology);
        }
    }

    private void checkProfile(OWLOntology ontology, OWLProfile profile,
            boolean shouldBeInProfile) {
        OWLProfileReport report = profile.checkOntology(ontology);
        assertEquals(
                "FAIL: " + ontology.getOntologyID() + " should "
                        + (!shouldBeInProfile ? "not " : "") + "be in the "
                        + profile.getName() + " profile. Report: " + report,
                shouldBeInProfile, report.isInProfile());
    }
}
