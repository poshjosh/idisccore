package com.idisc.core;

public abstract interface AppProperties
{
  public static final String NIGERIAN_NEWSMEDIA = "nigerian_newsmedia";
  public static final String BASE_URL = "baseURL";
  public static final String MAX_FEED_AGE_DAYS = "maxFeedAgeDays";
  public static final String ARCHIVE_BATCH_SIZE = "archiveBatchSize";
  public static final String REQUESTHANDLER_PROVIDER = "requesthandlerFactory";
  public static final String RSS_TIMEOUT_PER_TASK_SECONDS = "rss.timeoutPerTaskSeconds";
  public static final String WEB_TIMEOUT_PER_TASK_SECONDS = "web.timeoutPerTaskSeconds";
  public static final String RSS_TIMEOUT_PER_SITE_SECONDS = "rss.timeoutPerSiteSeconds";
  public static final String WEB_TIMEOUT_PER_SITE_SECONDS = "web.timeoutPerSiteSeconds";
  public static final String MAXCONCURRENT = "maxConcurrentSites";
  public static final String TOLERANCE = "dataComparisonTolerance";
  public static final String MAX_FAILS_ALLOWED = "maxFailsAllowedPerSite";
}
