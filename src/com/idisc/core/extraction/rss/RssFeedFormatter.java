package com.idisc.core.extraction.rss;

import com.sun.syndication.feed.synd.SyndFeed;




public class RssFeedFormatter
{
  public SyndFeed format(SyndFeed feed)
  {
    if (feed.getLink() == null) { return feed;
    }
    String link = feed.getLink().trim();
    
    if (link.startsWith("http://www.newswatchngr.com")) {
      if ((feed.getTitle() != null) && (feed.getTitle().trim().toLowerCase().startsWith("joomla!"))) {
        feed.setTitle("Newswatch Ngr");
      }
      if ((feed.getDescription() != null) && (feed.getDescription().trim().toLowerCase().startsWith("joomla!"))) {
        feed.setDescription("News feed from Newswatch Nigeria");
      }
    } else if ((link.startsWith("http://www.dailytrust.dailytrust.com")) && 
      (feed.getTitle() != null) && (feed.getTitle().trim().toLowerCase().equals("news"))) {
      feed.setTitle("Daily Trust");
      feed.setDescription("News feed from Daily Trust");
    }
    

    return feed;
  }
}
