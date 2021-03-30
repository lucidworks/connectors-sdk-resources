package com.lucidworks.connector.plugins.security.fetcher;

import com.lucidworks.connector.plugins.security.config.SecurityFilteringConfig;
import com.lucidworks.connector.plugins.security.generator.DocumentGenerator;
import com.lucidworks.connector.plugins.security.model.Permission;
import com.lucidworks.connector.plugins.security.model.SecurityDocument;
import com.lucidworks.connector.plugins.security.util.DocumentType;
import com.lucidworks.connector.plugins.security.util.SecurityFilteringConstants;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StartResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.lucidworks.connector.plugins.security.util.SecurityFilteringConstants.ACCESS_CONTROL;

public class SecurityFilteringContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringContentFetcher.class);

  private final DocumentGenerator documentGenerator;
  private final SecurityFilteringConfig config;

  @Inject
  public SecurityFilteringContentFetcher(SecurityFilteringConfig config, DocumentGenerator documentGenerator) {
    this.config = config;
    this.documentGenerator = documentGenerator;
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    if (!input.hasId()) {
      logger.info("Initial crawl");
      AtomicInteger index = new AtomicInteger(1);
      IntStream.rangeClosed(1, config.properties().typeADocuments())
          .forEach(indexA -> emitCandidate(fetchContext, DocumentType.DOCUMENT_TYPE_A, index.getAndIncrement()));
      IntStream.rangeClosed(1, config.properties().typeBDocuments())
          .forEach(indexB -> emitCandidate(fetchContext, DocumentType.DOCUMENT_TYPE_B, index.getAndIncrement()));
      IntStream.rangeClosed(1, config.properties().typeCDocuments())
          .forEach(indexC -> emitCandidate(fetchContext, DocumentType.DOCUMENT_TYPE_C, index.getAndIncrement()));
      IntStream.rangeClosed(1, config.properties().typeDDocuments())
          .forEach(indexD -> emitCandidate(fetchContext, DocumentType.DOCUMENT_TYPE_D, index.getAndIncrement()));

      logger.info("Generated [{}] candidates", index.get());
      //emit the default users and group
      fetchContext.newCandidate(SecurityFilteringConstants.USER_A)
          .withTargetPhase(ACCESS_CONTROL)
          .metadata(m -> m.setString(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.USER_TYPE))
          .emit();

      fetchContext.newCandidate(SecurityFilteringConstants.USER_B)
          .withTargetPhase(ACCESS_CONTROL)
          .metadata(m -> {
            m.setString(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.USER_TYPE);
            m.setString(SecurityFilteringConstants.PARENTS, SecurityFilteringConstants.GROUP_B);
          })
          .emit();

      fetchContext.newCandidate(SecurityFilteringConstants.USER_C)
          .withTargetPhase(ACCESS_CONTROL)
          .metadata(m -> m.setString(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.USER_TYPE))
          .emit();

      fetchContext.newCandidate(SecurityFilteringConstants.GROUP_B)
          .withTargetPhase(ACCESS_CONTROL)
          .metadata(m -> m.setString(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.GROUP_TYPE))
          .emit();
      return fetchContext.newResult();
    }
    Optional<SecurityDocument> document = documentGenerator.generate(input.getId(), input.getMetadata());
    document.ifPresent(securityDocument -> {
      fetchContext.newDocument()
          .fields(f -> f.merge(securityDocument.getFields()))
          .withACLs(securityDocument.getPermissions()
              .stream()
              .map(Permission::getId)
              .collect(Collectors.toList()))
          .emit();

      for (Permission permission : securityDocument.getPermissions()) {
        fetchContext.newCandidate(permission.getId())
            .withTargetPhase(ACCESS_CONTROL)
            .metadata(m -> {
              m.setString(SecurityFilteringConstants.ASSIGNED, permission.getAssigned());
              m.setString(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.PERMISSION_TYPE);
            })
            .emit();
      }
    });
    return fetchContext.newResult();
  }

  private void emitCandidate(FetchContext fetchContext, DocumentType documentType, int index) {
    fetchContext.newCandidate(String.format("item-%d", index))
        .metadata(m -> {
          m.setString(SecurityFilteringConstants.TYPE, documentType.name());
          m.setInteger("index", index);
        })
        .emit();
  }
}
