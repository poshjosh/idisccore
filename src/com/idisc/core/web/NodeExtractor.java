package com.idisc.core.web;

import com.bc.util.XLogger;
import com.bc.webdatex.filter.NodeVisitingFilterIx;
import com.bc.webdatex.locator.TagLocator;
import com.bc.webdatex.locator.TagLocatorIx;
import com.idisc.core.IdiscApp;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import com.scrapper.util.PageNodes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class NodeExtractor {
    
  private float tolerance;
  private String sitename;
  private String[] dp_accessViaGetter;
  private CapturerContext cfg_accessViaGetter;
  
  public NodeExtractor() {}
  
  public NodeExtractor(float tolerance, String sitename){
    this.tolerance = tolerance;
    this.sitename = sitename;
  }
  
  public Map<String, String> extract(PageNodes pageNodes) {
      
    Map<String, String> output = null;
    
    CapturerContext context = getContext();
    
    if (context == null) {
      throw new NullPointerException();
    }
    
    final int MAX = 20;
    
    for (int i = 0; i < 10; i++)
    {
      Object targetProps = context.getConfig().getObject(new Object[] { "targetNode" + i });
      
      if (targetProps == null)
      {
        if (i != 0)
          break;
        NodeList targetNodes = pageNodes.getBody().getChildren();
        
        output = Collections.singletonMap("content", targetNodes.toHtml());
        
        break;
      }
      


      Map.Entry<String, String> entry;
      

      try
      {
        entry = extract(pageNodes, i);
      }
      catch (ParserException e)
      {
        XLogger.getInstance().log(Level.WARNING, "Parse failed", getClass(), e);
        
        NodeList targetNodes = pageNodes.getBody().getChildren();
        
        entry = newEntry("content", targetNodes.toHtml());
        
        i = 10;
      }
      
      if (output == null) {
        output = new HashMap();
      }
      
      output.put(entry.getKey(), entry.getValue());
    }
    
    return output;
  }
  
  public Map.Entry<String, String> extract(PageNodes pageNodes, int index)
    throws ParserException {
      
    CapturerContext context = getContext();
    
    final String name = "targetNode" + index;
    
    Object targetProps = context.getConfig().getObject(new Object[] { name });
    
    if (targetProps == null) {
      throw new NullPointerException();
    }
    
    MultipleNodesExtractorIx pageExtractor = context.getExtractor();
    
    com.bc.webdatex.extractor.NodeExtractor nodeExtractor = pageExtractor.getExtractor(name);
    
    TagLocator tagLocator = nodeExtractor.getFilter().getTagLocator();
    
    NodeList nodeList = pageNodes.getNodeList();
    
    nodeList.visitAllNodesWith(tagLocator);
    
    Tag targetNode = tagLocator.getTarget();
    
    if (targetNode != null) {
        
      XLogger.getInstance().log(Level.FINER, "Found directly: {0} = {1}", getClass(), name, targetNode.toTagHtml());
      
      NodeList targetNodes = new NodeList();
      targetNodes.add(targetNode);
      
      nodeList = targetNodes;
    }
    
    nodeExtractor.setEnabled(true);
    
    updateTolerance(nodeExtractor.getFilter());
    
    nodeList.visitAllNodesWith(nodeExtractor);
    
    String key = context.getSettings().getColumns(name)[0];
    String val = nodeExtractor.getExtract().toString();
    
    XLogger.getInstance().log(Level.FINER, "Extracted: {0}={1}", getClass(), key, val);
    
    return newEntry(key, val);
  }
  
  private void updateTolerance(NodeVisitingFilterIx nodeVisitingFilter) {
    TagLocatorIx tagLocator = nodeVisitingFilter.getTagLocator();
    if (tagLocator != null) {
      tagLocator.setTolerance(this.tolerance);
    }
  }
  
  private Map.Entry<String, String> newEntry(final String key, final String val) {
    return new Map.Entry<String, String>()
    {
      @Override
      public String getKey() {
        return key;
      }
      
      @Override
      public String getValue() {
        return val;
      }
      
      @Override
      public String setValue(String value) {
        throw new UnsupportedOperationException("Not supported.");
      }
    };
  }
  
  public String[] getDatePatterns()
  {
    if (this.dp_accessViaGetter == null) {
      Object[] arr = getContext().getConfig().getArray(new Object[] { Config.Formatter.datePatterns });
      if ((arr == null) || (arr.length == 0))
      {

        this.dp_accessViaGetter = new String[0];
      } else {
        this.dp_accessViaGetter = new String[arr.length];
        System.arraycopy(arr, 0, this.dp_accessViaGetter, 0, arr.length);
      }
    }
    return this.dp_accessViaGetter;
  }
  
  public CapturerContext getContext()
  {
    if (this.sitename == null) {
      return null;
    }
    if (this.cfg_accessViaGetter == null) {
      ScrapperConfigFactory factory = IdiscApp.getInstance().getCapturerApp().getConfigFactory();
      this.cfg_accessViaGetter = factory.getContext(this.sitename);
    }
    return this.cfg_accessViaGetter;
  }
  
  public String getSitename() {
    return this.sitename;
  }
  
  public void setSitename(String sitename) {
    this.sitename = sitename;
    this.cfg_accessViaGetter = null;
  }
  
  public float getTolerance() {
    return this.tolerance;
  }
  
  public void setTolerance(float tolerance) {
    this.tolerance = tolerance;
  }
}
