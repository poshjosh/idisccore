package com.idisc.core.web;

import com.bc.webdatex.filter.NodeVisitingFilterIx;
import com.bc.webdatex.locator.TagLocatorIx;
import com.bc.util.XLogger;
import com.bc.webdatex.locator.TagLocator;
import com.idisc.core.IdiscApp;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.config.Config;
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

//import com.bc.wde.transverse.TransverseFilterIx;
//import com.bc.wde.transverse.Transverser;
//import com.scrapper.extractor.NodeExtractor;
/**
 * @(#)NodeExtractorMgr.java   09-Jan-2015 23:58:17
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class NodeExtractor {

    private float tolerance;
    private String sitename;

    public NodeExtractor() { }
    
    public NodeExtractor(float tolerance, String sitename) {
        this.tolerance = tolerance;
        this.sitename = sitename;
    }
    
    public Map<String, String> extract(PageNodes pageNodes) {
        
        Map<String, String> output = null;
        
        CapturerContext context = this.getContext();
        
        if(context == null) {
            throw new NullPointerException();
        }
        
        final int MAX = 10;
        
        for(int i=0; i<MAX; i++) {
            
            Object targetProps = context.getConfig().getObject("targetNode"+i);

            if(targetProps == null) {

                if(i == 0) {
                    
                    NodeList targetNodes = pageNodes.getBody().getChildren();

                    output = Collections.singletonMap("content", targetNodes.toHtml());
                    
                }
                    
                break;
                
            }else{
                
                Map.Entry<String, String> entry;
                
                try{
                    
                    entry = this.extract(pageNodes, i);
                
                }catch(ParserException e) {

                    XLogger.getInstance().log(Level.WARNING, "Parse failed", this.getClass(), e);

                    NodeList targetNodes = pageNodes.getBody().getChildren();
                    
                    entry = this.newEntry("content", targetNodes.toHtml());
                    
                    // This will cause the for loop to end after this iteration
                    i = MAX;
                }
                
                if(output == null) {
                    output = new HashMap<>();
                }
                
                output.put(entry.getKey(), entry.getValue());
            }
        } 
        
        return output;
    }
    
    public Map.Entry<String, String> extract(
            PageNodes pageNodes, int index) 
            throws ParserException {
        
        final String key;
        final String val;
        
        CapturerContext context = this.getContext();
        
        String name = "targetNode" + index;
        
        Object targetProps = context.getConfig().getObject(name);

        if(targetProps == null) {
            
            throw new NullPointerException();
            
        }else{
            
            MultipleNodesExtractorIx pageExtractor = context.getExtractor();
            
            com.bc.webdatex.extractor.NodeExtractor nodeExtractor = pageExtractor.getExtractor(name);

            TagLocator tagLocator = nodeExtractor.getFilter().getTagLocator();
            
            NodeList nodeList = pageNodes.getNodeList();
            
            nodeList.visitAllNodesWith(tagLocator);
            
            Tag targetNode = tagLocator.getTarget();
            
            if(targetNode != null) {
                
XLogger.getInstance().log(Level.FINER, "Found directly: {0} = {1}", 
this.getClass(), name, targetNode.toTagHtml());
                
                NodeList targetNodes = new NodeList();
                targetNodes.add(targetNode);
                
                nodeList = targetNodes;
            }
            
            nodeExtractor.setEnabled(true);
            
            this.updateTolerance(nodeExtractor.getFilter());
            
            nodeList.visitAllNodesWith(nodeExtractor);

            key = context.getSettings().getColumns(name)[0];
            val = nodeExtractor.getExtract().toString();
        }

XLogger.getInstance().log(Level.FINER, "Extracted: {0}={1}", this.getClass(), key, val);
        return newEntry(key, val);
    }
    
    private void updateTolerance(NodeVisitingFilterIx nodeVisitingFilter) {
        TagLocatorIx tagLocator = nodeVisitingFilter.getTagLocator();
        if(tagLocator != null) {
            tagLocator.setTolerance(tolerance);
        }
    }

    private Map.Entry<String, String> newEntry(final String key, final String val) {
        return new Map.Entry<String, String>(){
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
    
    private String [] dp_accessViaGetter;
    public String [] getDatePatterns() {
        if(dp_accessViaGetter == null) {
            Object [] arr = this.getContext().getConfig().getArray(Config.Formatter.datePatterns);
            if(arr == null || arr.length == 0) {
                // We use a null value to signify that this has not been initialized
                // So we set this to an empty array
                dp_accessViaGetter = new String[0];
            }else{    
                dp_accessViaGetter = new String[arr.length];
                System.arraycopy(arr, 0, dp_accessViaGetter, 0, arr.length);
            }
        }
        return dp_accessViaGetter;
    }
    
    private CapturerContext cfg_accessViaGetter;
    public CapturerContext getContext() {
        if(sitename == null) {
            return null;
        }
        if(this.cfg_accessViaGetter == null) {
            ScrapperConfigFactory factory = IdiscApp.getInstance().getCapturerApp().getConfigFactory();
            this.cfg_accessViaGetter = factory.getContext(sitename);
        }
        return this.cfg_accessViaGetter;
    }

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = sitename;
        this.cfg_accessViaGetter = null;
    }

    public float getTolerance() {
        return tolerance;
    }

    public void setTolerance(float tolerance) {
        this.tolerance = tolerance;
    }
}
