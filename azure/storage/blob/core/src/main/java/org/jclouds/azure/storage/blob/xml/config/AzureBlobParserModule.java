package org.jclouds.azure.storage.blob.xml.config;

import org.jclouds.azure.storage.blob.domain.ContainerMetadata;
import org.jclouds.azure.storage.blob.xml.AccountNameEnumerationResultsHandler;
import org.jclouds.azure.storage.domain.MetadataList;
import org.jclouds.azure.storage.xml.AzureStorageParserFactory;
import org.jclouds.azure.storage.xml.config.AzureStorageParserModule;
import org.jclouds.command.ConfiguresResponseTransformer;
import org.jclouds.http.functions.ParseSax;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryProvider;

/**
 * Creates the factories needed to interpret Azure Blob Service responses
 * 
 * @author Adrian Cole
 */
@ConfiguresResponseTransformer
public class AzureBlobParserModule extends AzureStorageParserModule {
   protected final TypeLiteral<AzureStorageParserFactory.GenericParseFactory<MetadataList<ContainerMetadata>>> accountNameEnumerationResultsHandler = new TypeLiteral<AzureStorageParserFactory.GenericParseFactory<MetadataList<ContainerMetadata>>>() {
   };

   @Override
   protected void bindParserImplementationsToReturnTypes() {
      super.bindParserImplementationsToReturnTypes();
      bind(new TypeLiteral<ParseSax.HandlerWithResult<MetadataList<ContainerMetadata>>>() {
      }).to(AccountNameEnumerationResultsHandler.class);
   }

   @Override
   protected void bindCallablesThatReturnParseResults() {
      super.bindCallablesThatReturnParseResults();
      bind(accountNameEnumerationResultsHandler).toProvider(
               FactoryProvider.newFactory(accountNameEnumerationResultsHandler,
                        new TypeLiteral<ParseSax<MetadataList<ContainerMetadata>>>() {
                        }));
   }

}