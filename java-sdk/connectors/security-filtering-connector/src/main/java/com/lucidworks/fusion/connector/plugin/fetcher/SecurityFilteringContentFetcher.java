package com.lucidworks.fusion.connector.plugin.fetcher;

import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import com.lucidworks.fusion.connector.plugin.generator.DocumentGenerator;
import com.lucidworks.fusion.connector.plugin.model.Permission;
import com.lucidworks.fusion.connector.plugin.model.SecurityDocument;
import com.lucidworks.fusion.connector.plugin.util.DocumentType;
import com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.ACCESS_CONTROL;

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
  public PreFetchResult preFetch(PreFetchContext preFetchContext) {
    AtomicInteger index = new AtomicInteger(1);

    IntStream.rangeClosed(1, config.properties().typeADocuments())
        .forEach(indexA -> emitCandidate(preFetchContext, DocumentType.DOCUMENT_TYPE_A, index.getAndIncrement()));

    IntStream.rangeClosed(1, config.properties().typeBDocuments())
        .forEach(indexB -> emitCandidate(preFetchContext, DocumentType.DOCUMENT_TYPE_B, index.getAndIncrement()));

    IntStream.rangeClosed(1, config.properties().typeCDocuments())
        .forEach(indexC -> emitCandidate(preFetchContext, DocumentType.DOCUMENT_TYPE_C, index.getAndIncrement()));

    IntStream.rangeClosed(1, config.properties().typeDDocuments())
        .forEach(indexD -> emitCandidate(preFetchContext, DocumentType.DOCUMENT_TYPE_D, index.getAndIncrement()));
    logger.info("Generated [{}] candidates", index.get());
    return preFetchContext.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    Optional<SecurityDocument> document = documentGenerator.generate(input.getId(), input.getMetadata());
    document.ifPresent(securityDocument -> {
      fetchContext.newDocument()
          .withFields(securityDocument.getFields())
          .withACLs(securityDocument.getPermissions()
              .stream()
              .map(Permission::getId)
              .collect(Collectors.toList()))
          .emit();

      for (Permission permission : securityDocument.getPermissions()) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(SecurityFilteringConstants.ASSIGNED, permission.getAssigned());
        metadata.put(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.PERMISSION_TYPE);
        fetchContext.newCandidate(permission.getId()).withTargetPhase(ACCESS_CONTROL).withMetadata(metadata).emit();
      }
    });
    return fetchContext.newResult();
  }

  private void emitCandidate(PreFetchContext preFetchContext, DocumentType documentType, int index) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put(SecurityFilteringConstants.TYPE, documentType.name());
    metadata.put("index", index);
    preFetchContext.newCandidate(String.format("item-%d", index)).withMetadata(metadata).emit();
  }
}
