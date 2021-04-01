package com.lucidworks.connector.plugins.security.fetcher;

import com.lucidworks.connector.plugins.security.config.SecurityFilteringConfig;
import com.lucidworks.connector.plugins.security.generator.DocumentGenerator;
import com.lucidworks.connector.plugins.security.model.Permission;
import com.lucidworks.connector.plugins.security.model.SecurityDocument;
import com.lucidworks.connector.plugins.security.util.SecurityConstants;
import com.lucidworks.connector.plugins.security.util.DocumentType;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.lucidworks.connector.plugins.security.util.SecurityConstants.ACCESS_CONTROL;

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
    if (isInitialCrawl(input)) {
      logger.info("Initial crawl");
      seedCandidates(fetchContext);
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
              m.setString(SecurityConstants.ASSIGNED, permission.getAssigned());
              m.setString(SecurityConstants.TYPE, SecurityConstants.PERMISSION_TYPE);
            })
            .emit();
      }
    });
    return fetchContext.newResult();
  }

  private boolean isInitialCrawl(final FetchInput input) {
    return !input.hasId();
  }

  private void seedCandidates(final FetchContext fetchContext) {
    logger.info("Emitting content candidates");
    AtomicInteger index = new AtomicInteger(1);
    IntStream.rangeClosed(1, config.properties().typeADocuments())
        .forEach(indexA -> emitDocumentCandidate(fetchContext, DocumentType.DOCUMENT_TYPE_A, index.getAndIncrement()));
    IntStream.rangeClosed(1, config.properties().typeBDocuments())
        .forEach(indexB -> emitDocumentCandidate(fetchContext, DocumentType.DOCUMENT_TYPE_B, index.getAndIncrement()));
    IntStream.rangeClosed(1, config.properties().typeCDocuments())
        .forEach(indexC -> emitDocumentCandidate(fetchContext, DocumentType.DOCUMENT_TYPE_C, index.getAndIncrement()));
    IntStream.rangeClosed(1, config.properties().typeDDocuments())
        .forEach(indexD -> emitDocumentCandidate(fetchContext, DocumentType.DOCUMENT_TYPE_D, index.getAndIncrement()));
    logger.info("Generated [{}] candidates", index.get());

    logger.info("Emitting ACL hierarchy candidates: users and a group");
    fetchContext.newCandidate(SecurityConstants.USER_A)
                .withTargetPhase(ACCESS_CONTROL)
                .metadata(m -> m.setString(SecurityConstants.TYPE, SecurityConstants.USER_TYPE))
                .emit();

    fetchContext.newCandidate(SecurityConstants.USER_B)
                .withTargetPhase(ACCESS_CONTROL)
                .metadata(m -> {
                  m.setString(SecurityConstants.TYPE, SecurityConstants.USER_TYPE);
                  m.setString(SecurityConstants.PARENTS, SecurityConstants.GROUP_B);
                })
                .emit();

    fetchContext.newCandidate(SecurityConstants.USER_C)
                .withTargetPhase(ACCESS_CONTROL)
                .metadata(m -> m.setString(SecurityConstants.TYPE, SecurityConstants.USER_TYPE))
                .emit();

    fetchContext.newCandidate(SecurityConstants.GROUP_B)
                .withTargetPhase(ACCESS_CONTROL)
                .metadata(m -> m.setString(SecurityConstants.TYPE, SecurityConstants.GROUP_TYPE))
                .emit();
  }

  private void emitDocumentCandidate(FetchContext fetchContext, DocumentType documentType, int index) {
    fetchContext.newCandidate(String.format("item-%d", index))
        .metadata(m -> {
          m.setString(SecurityConstants.TYPE, documentType.name());
          m.setInteger("index", index);
        })
        .emit();
  }
}
