package com.idisc.core.extraction.web;

import com.bc.nodelocator.ConfigName;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import org.htmlparser.util.ParserException;
import java.util.List;
import org.htmlparser.dom.HtmlDocument;
import java.util.Arrays;
import java.util.logging.Logger;
import com.bc.webdatex.extractors.PageExtractor;
import java.util.Collections;
import com.bc.webdatex.context.ExtractionContext;
import com.bc.webdatex.context.ExtractionConfig;

public class WebFeedDataExtractor {

  private transient static final Logger LOG = Logger.getLogger(WebFeedDataExtractor.class.getName());
    
  private final float tolerance;

  private final int bufferSize;
  
  private final ExtractionContext capturerContext;
  
  public WebFeedDataExtractor(float tolerance, ExtractionContext context, int bufferSize){
    this.tolerance = tolerance;
    this.capturerContext = Objects.requireNonNull(context);
    this.bufferSize = bufferSize;
  }
  
  public Map<String, String> extract(HtmlDocument pageNodes) {
      
    Map<String, String> output = null;
    
    final List selectorCfgList = capturerContext.getConfig().getList(ConfigName.selectorConfigList);
    
    for (int i=0; i<selectorCfgList.size(); i++) {
        
      final Integer id = i;
      
      final ExtractionConfig settings = capturerContext.getExtractionConfig();

      final boolean exists = output != null && output.containsKey(id);
      
      final boolean append = settings.isConcatenateMultipleExtracts(id, false);
      
      final boolean doExtract = !exists || exists && append;
      
      if(!doExtract) {
        continue;
      }
      
      String [] columns = settings.getColumns(id);
      
      String value;
      
      try {
          
//        final Map selectorCfg = (Map)selectorCfgList.get(id);

        final StringBuilder extract = extract(id, pageNodes, null);

        value = extract == null ? null : extract.toString();

      } catch (ParserException e) {

        value = null;
        
        LOG.log(Level.WARNING, "Extraction failed for : " + id +
                ", columns: " + (columns==null?null:Arrays.toString(columns)) +
                " in nodes extracted from: "+pageNodes.getURL(), e);
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
    
    return output == null || output.isEmpty() ? Collections.EMPTY_MAP : output;
  }
  
  public StringBuilder extract(Object id, HtmlDocument doc, StringBuilder outputIfNone) throws ParserException {
      
    final StringBuilder extract = this.extract(id, capturerContext.getExtractor(), doc, outputIfNone);
    
    return extract;
  }
  
  public StringBuilder extract(
          Object id, PageExtractor pageExtractor, 
          HtmlDocument doc, StringBuilder outputIfNone) 
          throws ParserException {
    
    com.bc.webdatex.extractors.node.NodeExtractor nodeExtractor = pageExtractor.getNodeExtractor(id);
    
    final StringBuilder extract;
    
    if(nodeExtractor == null) {
      
      LOG.log(Level.WARNING, () -> "Failed to create instance of " + 
              com.bc.webdatex.extractors.node.NodeExtractor.class.getName() + 
              "for " + id  + " of " + doc.getURL());
      
      extract = null;
      
    }else{
    
      nodeExtractor.setEnabled(true);
    
      doc.getElements().visitAllNodesWith(nodeExtractor);
    
      extract = nodeExtractor.getExtract();
    }
    
    return extract == null ? outputIfNone : extract;
  }

  public final ExtractionContext getContext() {
    return this.capturerContext;
  }
  
  public final String getSitename() {
    return this.capturerContext.getConfig().getName();
  }
  
  public final float getTolerance() {
    return this.tolerance;
  }
}
/**
 * 
  private String getMainContent(HtmlDocument pageNodes, String outputIfNone) {
    final BodyTag bodyTag = pageNodes.getBody();
    final List<Node> nodes = bodyTag == null ? pageNodes.getElements() : bodyTag.getChildren();
    try{
      final int minSize = pageNodes.getTitle() == null ? 0 : pageNodes.getTitle().getTitle() == null ? 0 : pageNodes.getTitle().getTitle().length();
      final Node mainNode = nodes == null ? null : this.getNodeWithLargestContent(nodes, minSize, null);
      return mainNode == null ? outputIfNone : mainNode.toHtml();
    }catch(ParserException ignored) {
      return outputIfNone;
    }
  }
  
  private Node getNodeWithLargestContent(List<Node> nodes, int minSize, Node outputIfNone) throws ParserException {
        
    NodeFilter filter = new NotFilter(
            new OrFilter(
                    new NodeClassFilter(ScriptTag.class), 
                    new NodeClassFilter(StyleTag.class)
            )
    );
      
    NodeSelector nodeSelector = new NodeSelector(filter, bufferSize, minSize, 0);
        
    return nodeSelector.select(nodes, outputIfNone);
  }
  
 * 
 */