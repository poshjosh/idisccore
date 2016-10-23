package com.idisc.core.web;

import com.bc.util.XLogger;
import com.scrapper.context.CapturerContext;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import com.bc.webdatex.nodedata.Dom;
import com.scrapper.context.CapturerSettings;
import com.bc.webdatex.locator.TagLocator;
import com.bc.webdatex.locator.impl.TagLocatorImpl;
import com.bc.webdatex.locator.impl.TransverseNodeMatcherImpl;
import com.bc.webdatex.nodefilter.NodeVisitingFilter;

public class NodeExtractor {
    
  private final float tolerance;

  private final CapturerContext capturerContext;
  
  public NodeExtractor(float tolerance, CapturerContext context){
    this.tolerance = tolerance;
    this.capturerContext = Objects.requireNonNull(context);
  }
  
  public Map<String, String> extract(Dom pageNodes) {
      
    Map<String, String> output = null;
    
    final int MAX = 20;
    
    for (int i = 0; i < MAX; i++) {
        
      final Object targetProps = capturerContext.getConfig().getObject(new Object[] { "targetNode" + i });
      
      if (targetProps == null) {
          
        if (i == 0) {
            
          final String content = this.getContent(pageNodes, null);
        
          if(content != null) {
            output = Collections.singletonMap("content", content);
          }else{
            output = Collections.EMPTY_MAP;  
          }
        }
        
        break;
      }
      
      final String name = "targetNode" + i;
      
      final CapturerSettings settings = capturerContext.getSettings();

      final boolean exists = output != null && output.containsKey(name);
      
      final boolean append = settings.isConcatenateMultipleExtracts(name, false);
      
      final boolean doExtract = !exists || exists && append;
      
      if(!doExtract) {
        continue;
      }
      
      String [] columns = settings.getColumns(name);
      
      String value;
      
      try {

        final StringBuilder extract = extract(pageNodes, name);

        value = extract == null ? null : extract.toString();

      } catch (ParserException e) {

        XLogger.getInstance().log(Level.WARNING, "Parse failed", getClass(), e);

        String content = this.getContent(pageNodes, null);

        if(content != null) {

          columns = new String[]{"content"};

          value = content;

          i = MAX;

        }else{

          value = null;
        }
      }
      
      if (output == null) {
        output = new HashMap();
      }

      if(value != null && !value.isEmpty()) {
          
        for(String column:columns) {
            
          if(append) {
              
            String prev = output.get(column);
            
            if(prev != null && !prev.isEmpty()) {
              output.put(column, prev + settings.getPartSeparator() + value);  
            }else{
              output.put(column, value);    
            }
          }else{
            output.put(column, value);  
          }
        }
      }
      
    }
    
    return output;
  }
  
  public String getContent(Dom pageNodes, String outputIfNone) {
    String content;
    BodyTag bodyTag = pageNodes.getBody();
    if(bodyTag == null) {
      NodeList nodes = pageNodes.getNodeList();
      content = nodes == null ? null : nodes.toHtml();
    }else{
      content = bodyTag.toHtml();
    }
    return content == null ? outputIfNone : content;
  }
  
  public StringBuilder extract(Dom pageNodes, String name) throws ParserException {
      
    Object targetProps = capturerContext.getConfig().getObject(new Object[] { name });
    
    if (targetProps == null) {
      throw new NullPointerException();
    }
    
    MultipleNodesExtractorIx pageExtractor = capturerContext.getExtractor();
    
    com.bc.webdatex.extractor.node.NodeExtractor nodeExtractor = pageExtractor.getExtractor(name);
    
//    TagLocatorImpl tagLocator = nodeExtractor.getFilter().getTagLocator();
    
    NodeList nodeList = pageNodes.getNodeList();
    
//    nodeList.visitAllNodesWith(tagLocator);
    
//    Tag targetNode = tagLocator.getTarget();
    
//    if (targetNode != null) {
        
//      XLogger.getInstance().log(Level.FINER, "Found directly: {0} = {1}", getClass(), name, targetNode.toTagHtml());

//      NodeList targetNodes = new NodeList();
//      targetNodes.add(targetNode);
      
//      nodeList = targetNodes;
//    }
    
    nodeExtractor.setEnabled(true);
    
    updateTolerance(nodeExtractor.getFilter());
    
    nodeList.visitAllNodesWith(nodeExtractor);
    
    StringBuilder extract = nodeExtractor.getExtract();
    
    return extract;
  }
  
  private void updateTolerance(NodeVisitingFilter nodeVisitingFilter) {
    TagLocator tagLocator = nodeVisitingFilter.getTagLocator();
    if (tagLocator != null) {
      tagLocator = new TagLocatorImpl(
              tagLocator.getId(), 
              tagLocator.getTransverse(), 
              new TransverseNodeMatcherImpl(this.tolerance));
      nodeVisitingFilter.setTagLocator(tagLocator);
    }
  }
  
  public final CapturerContext getContext() {
    return this.capturerContext;
  }
  
  public final String getSitename() {
    return this.capturerContext.getConfig().getName();
  }
  
  public final float getTolerance() {
    return this.tolerance;
  }
}
