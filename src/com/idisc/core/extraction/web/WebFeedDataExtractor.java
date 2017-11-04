package com.idisc.core.extraction.web;

import com.bc.util.XLogger;
import com.bc.webdatex.extractor.node.NodeSelector;
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
import com.bc.webdatex.locator.TagLocator;
import com.bc.webdatex.locator.impl.TagLocatorImpl;
import com.bc.webdatex.locator.impl.TransverseNodeMatcherImpl;
import com.bc.webdatex.nodefilter.NodeVisitingFilter;
import java.util.List;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.StyleTag;
import com.bc.webdatex.extractor.node.NodeExtractorConfig;
import com.bc.dom.HtmlDocument;

public class WebFeedDataExtractor {
    
  private final float tolerance;

  private final int bufferSize;
  
  private final CapturerContext capturerContext;
  
  public WebFeedDataExtractor(float tolerance, CapturerContext context, int bufferSize){
    this.tolerance = tolerance;
    this.capturerContext = Objects.requireNonNull(context);
    this.bufferSize = bufferSize;
  }
  
  public Map<String, String> extract(HtmlDocument pageNodes) {
      
    Map<String, String> output = null;
    
    final int MAX = 20;
    
    for (int i = 0; i < MAX; i++) {
        
      final String ID = "targetNode" + i;
      
      final Object targetProps = capturerContext.getConfig().getObject(new Object[] { ID });
      
      if (targetProps == null) {
        break;  
      }
      
      final NodeExtractorConfig settings = capturerContext.getNodeExtractorConfig();

      final boolean exists = output != null && output.containsKey(ID);
      
      final boolean append = settings.isConcatenateMultipleExtracts(ID, false);
      
      final boolean doExtract = !exists || exists && append;
      
      if(!doExtract) {
        continue;
      }
      
      String [] columns = settings.getColumns(ID);
      
      String value;
      
      try {

        final StringBuilder extract = extract(pageNodes, ID);

        value = extract == null ? null : extract.toString();

      } catch (ParserException e) {

        value = null;
        
        XLogger.getInstance().log(Level.WARNING, "Extraction failed for column: " + ID + 
                " in nodes extracted from: "+pageNodes.getURL(), getClass(), e);
      }
      
      if (output == null) {
        output = new HashMap();
      }

      if(value != null && !value.isEmpty()) {
          
        for(String column : columns) {
            
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
    
    final boolean extractDefaultContent = (output == null || (output.get("content") == null && output.get("description") == null));
    
    if(extractDefaultContent) {
        
      final String defaultContent = this.getMainContent(pageNodes, null);
      
      XLogger.getInstance().log(Level.FINE, "Extracted default content of length: {0}", 
              this.getClass(), (defaultContent==null?null:defaultContent.length()));
        
      if(defaultContent != null) {
      
        if(output == null) {
          output = Collections.singletonMap("content", defaultContent);
        } else{
          output.put("content", defaultContent);
        } 
      }
    }
    
    return output;
  }
  
  private String getMainContent(HtmlDocument pageNodes, String outputIfNone) {
    final BodyTag bodyTag = pageNodes.getBody();
    final List<Node> nodes = bodyTag == null ? pageNodes.getElements() : bodyTag.getChildren();
    try{
      final int minSize = pageNodes.getTitle() == null ? 0 : pageNodes.getTitle().getTitle() == null ? 0 : pageNodes.getTitle().getTitle().length();
      final Node mainNode = nodes == null ? null : this.getNodeWithLargestContent(nodes, minSize, null);
      return mainNode == null ? outputIfNone : mainNode.toHtml();
    }catch(ParserException e) {
      return outputIfNone;
    }
  }
  
  public Node getNodeWithLargestContent(List<Node> nodes, int minSize, Node outputIfNone) throws ParserException {
        
    NodeFilter filter = new NotFilter(
            new OrFilter(
                    new NodeClassFilter(ScriptTag.class), 
                    new NodeClassFilter(StyleTag.class)
            )
    );
      
    NodeSelector nodeSelector = new NodeSelector(filter, bufferSize, minSize, 0);
        
    return nodeSelector.select(nodes, outputIfNone);
  }
    
  public StringBuilder extract(HtmlDocument pageNodes, String name) throws ParserException {
      
    Object targetProps = capturerContext.getConfig().getObject(new Object[] { name });
    
    if (targetProps == null) {
      throw new NullPointerException();
    }
    
    MultipleNodesExtractorIx pageExtractor = capturerContext.getExtractor();
    
    com.bc.webdatex.extractor.node.NodeExtractor nodeExtractor = pageExtractor.getExtractor(name);
    
//    TagLocatorImpl tagLocator = nodeExtractor.getFilter().getTagLocator();
    
    NodeList nodeList = pageNodes.getElements();
    
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
