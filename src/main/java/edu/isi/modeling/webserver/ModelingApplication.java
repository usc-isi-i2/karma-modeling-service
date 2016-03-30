package edu.isi.modeling.webserver;

import io.swagger.jaxrs.config.BeanConfig;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

import edu.isi.modeling.config.ModelingConfiguration;
import edu.isi.modeling.metadata.GraphVizMetadata;
import edu.isi.modeling.metadata.JSONModelsMetadata;
import edu.isi.modeling.metadata.MetadataManager;
import edu.isi.modeling.metadata.ModelLearnerMetadata;
import edu.isi.modeling.metadata.OntologyMetadata;
import edu.isi.modeling.ontology.OntologyManager;

@ApplicationPath("/rest")
//@ApplicationPath("/")
public class ModelingApplication extends Application {

	private static String contextId;
	private static OntologyManager ontologyManager;
	
	
    public static String getContextId() {
		return contextId;
	}

    
	public static OntologyManager getOntologyManager() {
		return ontologyManager;
	}


	/**
     * Default constructor
     */
    public ModelingApplication() {
        super();

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("0.1");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
//        beanConfig.setBasePath("/ModelingRESTJersey");
        beanConfig.setBasePath("/KarmaModelingService/rest");
        beanConfig.setResourcePackage("edu.isi.modeling.resources");
        beanConfig.setScan(true);
        beanConfig.setPrettyPrint(true);

    }

    /**
     * Initialize the web application
     */
    @PostConstruct
    public static void initialize() {
		
		try {

			ContextParametersRegistry contextParametersRegistry = ContextParametersRegistry.getInstance();
			ContextParameterMap contextParameters = contextParametersRegistry.getContextParameters(ModelingConfiguration.getUserHomeDir());
		
			MetadataManager metadataManager = new MetadataManager(contextParameters);
//			metadataManager.register(new UserConfigMetadata(contextParameters));
			metadataManager.register(new OntologyMetadata(contextParameters));
			metadataManager.register(new JSONModelsMetadata(contextParameters));
			metadataManager.register(new GraphVizMetadata(contextParameters));
			metadataManager.register(new ModelLearnerMetadata(contextParameters));
		
			contextId = contextParameters.getId();
			ontologyManager = new OntologyManager(contextId);

			metadataManager.setup(contextId, ontologyManager);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

    /**
     * Define the set of "Resource" classes for the javax.ws.rs.core.Application
     */
    @Override
    public Set<Class<?>> getClasses() {

        Set<Class<?>> resources = new HashSet<Class<?>>();
        resources.addAll(getResources().getClasses());
        
        // swagger resources
        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        resources.add(edu.isi.modeling.resources.CORSFilter.class);
        
        return resources;
    }

    /**
     * Scans the project for REST resources using Jersey
     * @return the resource configuration information
     */
    public static ResourceConfig getResources() {
        // create a ResourceConfig that scans for all JAX-RS resources and providers in defined package
        final ResourceConfig config = new ResourceConfig().packages("edu.isi.modeling.resources");
        return config;
    }

}
