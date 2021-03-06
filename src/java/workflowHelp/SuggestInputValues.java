package workflowHelp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jdom.Namespace;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import ontologyManager.OntologyManager;
import parser.SchemaParser;

/**
 * A class that has methods for suggesting possible input values to the user 
 * for a particular Web service input.
 * 
 * @author Alok Dhamanaskar
 * @see LICENSE (MIT style license file).
 * 
*/
public class SuggestInputValues
{

    /**
     * A constructor for suggestInputValues class that given the name of the parameter,
     * URL for the WSDL file and URL for the Ontology file gets the list of possible input 
     * values by looking up individuals & direct subclasses.
     * Use getInputValues() to get the actual values.
     * 
     * @param WSDLURL : URL for the WSDL file from which the parameter is.
     * @param paramName : name of the parameter to suggest values for
     * @param owlURI : URI for the owl file
     * @return 
     */
    public static List<String> SuggestParamValues(String WSDLURL, String paramName, String owlURI)
    {
        List<String> values = new ArrayList<String>();  
        String paramIRI;
        
        Element paramElement= null;
        Namespace sawsdlNS = Namespace.getNamespace("sawsdl", "http://www.w3.org/ns/sawsdl");
        SchemaParser schemaParser = new SchemaParser();
        
        List<Element> schemaList = schemaParser.getSchemaElemList(WSDLURL);

        for(Element e : schemaList) {
            paramElement = schemaParser.getElementFromSchema(paramName, e);
        } // for
            
        org.jdom.Attribute attribute = paramElement.getAttribute("modelReference", sawsdlNS);

        if(attribute != null && !attribute.getValue().isEmpty()) {
              
                paramIRI = attribute.getValue();
                OntologyManager parser = OntologyManager.getInstance(owlURI);
                OWLClass conceptClass = parser.getConceptClass(paramIRI);
                        
                values = getIndividuals(conceptClass, parser);
                values.addAll(getDirectSubClasses(conceptClass, parser));

        } // if
        
        return values;
        
    }//Method ends
    
    /**
     *
     * Finds out the direct sub-classes for a given class in an Ontology
     *
     * @param conceptClass : The class in the ontology, as an object of OWLClass to find subclasses for
     * @param parser : Object of ontology Manager
     */
    private static List<String> getDirectSubClasses(OWLClass conceptClass, OntologyManager parser) {
        List<String> values = new ArrayList<String>();        
        Set<OWLClass> subclasses = parser.getDirectSubClasses(conceptClass);
        String label = "";
        String className = "";
        
        for (OWLClass c : subclasses) {
            label = parser.getClassLabel(c);
            className = parser.getConceptName(c.getIRI().toString());
            
            if (!label.isEmpty()) {
                values.add(label);
            } else if(!className.isEmpty()) {
                values.add(className);
            } // if
            
        } // for 
        
        return values;
    
    }//method ends

    /**
     *
     * Finds out the Individuals for a given class in an Ontology
     *
     * @param conceptClass : The class in the ontology, as an object of OWLClass to find subclasses for
     * @param parser : Object of ontology Manager
     */
    private static List<String> getIndividuals(OWLClass conceptClass, OntologyManager parser) {
        List<String> values = new ArrayList<String>();        
        try{
            OWLOntology ontology = parser.getOntology();
            Set<OWLIndividual> individuals =  conceptClass.getIndividuals(ontology);

            for(OWLIndividual i : individuals)
            {
                String name = i.toStringID();
                String[] temp;
                if (name.contains("#"))
                {
                       temp = name.split("#");
                       name = temp[1];
                }// if ends
                values.add(name);
            }//for ends
        }// try ends
        catch(Exception e)
        {
            System.out.println("Exception Occured when getting the Individuals: " + e);
        }
        return values;
    }// method ends
    
    public static void main (String[] args)
    {
        util.DebuggingUtils.printCollection(SuggestInputValues.SuggestParamValues("http://mango.ctegd.uga.edu/jkissingLab/SWS/Wsannotation/resources/wublast.sawsdl","program", "owl/webService.owl"));
        System.out.println("----------------------------------------------------------------");
        util.DebuggingUtils.printCollection(SuggestInputValues.SuggestParamValues("http://mango.ctegd.uga.edu/jkissingLab/SWS/Wsannotation/resources/wublast.sawsdl","exp", "owl/webService.owl"));
    }
    
}