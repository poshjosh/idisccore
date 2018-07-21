/*
 * Copyright 2016 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idisc.core;

import com.bc.webcrawler.UrlParser;
import com.bc.webdatex.context.CapturerContext;
import com.bc.webdatex.context.NodeExtractorConfig;
import java.time.LocalDateTime;
import com.bc.webdatex.extractors.PageExtractor;
import com.idisc.core.extraction.UrlParserImpl;
import java.io.IOException;
import org.htmlparser.dom.HtmlDocument;
import com.bc.webdatex.context.CapturerContextFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 19, 2016 2:15:30 PM
 */
public class ExtractionTestBase extends IdiscTestBase {
    
    public ExtractionTestBase() { }
    
    public NodeExtractorConfig getNodeExtractorConfig(String site) {
        
        CapturerContextFactory factory = this.getContextFactory();
        
        CapturerContext ctx = factory.getContext(site);
        
        return ctx.getNodeExtractorConfig();
    }
    
    public com.bc.webdatex.extractors.node.NodeExtractor getNodeExtractor(String site, Object key) {
        PageExtractor pageExtractor = getPageExtractor(site);
        com.bc.webdatex.extractors.node.NodeExtractor nodeExtractor = 
                ((PageExtractor)pageExtractor).getNodeExtractor(key);
        return nodeExtractor;
    }
    
    public PageExtractor getPageExtractor(String site) {
        CapturerContext ctx;
        ctx = this.getContextFactory().getContext(site);
        PageExtractor pageExtractor = ctx.getExtractor();
        return pageExtractor;
    }
    
    public HtmlDocument getNodes(String url) throws IOException {
        
        final UrlParser<HtmlDocument> parser = new UrlParserImpl();
        
        final HtmlDocument doc = parser.parse(url);
        
this.log(this.getClass(), "Nodes found: " + doc.size());

        return doc;
    }
    public String getUrl(String site, String outputIfNone) {
        final String [] urls = this.getUrls(site, null);

        final String url;
        if(urls == null || urls.length == 0) {
            url = outputIfNone;
        }else{
            int random = com.bc.util.Util.randomInt(urls.length);
            url = urls[random];
        }
log(this.getClass(), "Site: " + site + ", url: " + url);
        return url;
    }

    public String [] getUrls(String site, String[] outputIfNone) {
        String [] urls;
        switch(site) {
            case "leadership.ng":
                urls = new String[]{
                    "http://www.leadership.ng/business/555767/economy-the-cross-of-militancy",
                    "http://www.leadership.ng/business/555782/innoson-group-partners-chinese-consortium-to-invest-1bn-in-it-sector-targets-7000-jobs",
                    "http://www.leadership.ng/news/555775/ondo-2016-ijaws-reject-jegedes-deputy-back-akeredolu"
                };
                break;
            case "bellanaija":
                urls = new String[]{
                    "https://www.bellanaija.com/2016/06/get-inspired-with-forbes-list-of-amercias-richest-self-made-women-oprah-winfrey-beyonce-taylor-swift-sheryl-sandberg-more/",
                    "https://www.bellanaija.com/2016/03/ty-bello-is-bimpe-onakoyas-biggest-fan-read-her-inspiring-story-on-the-makeup-maestro/",
                    "https://www.bellanaija.com/2015/06/09/designer-deola-sagoe-is-a-vision-in-gold-in-her-own-piece/"
                };
                break;
            case "lindaikeji.blogspot":
                urls = new String []{
                    "https://www.lindaikejisblog.com/2017/9/dasuki-to-appear-as-olisa-metuhs-witness-in-alleged-n400m-fraud-case.html",
                    "https://www.lindaikejisblog.com/2017/9/uk-government-demands-clarification-on-ipob-leader-nnamdi-kanus-where-about-from-fg.html",
//                    "http://www.lindaikejisblog.com/2013/09/funke-akindele-denies-launching-jenifa.html",
//                    "http://www.lindaikejisblog.com/2016/05/okonjo-iweala-reacts-to-reports-that.html",
//                    "http://www.lindaikejisblog.com/2016/05/comedian-elenu-and-his-wife-jane-join.html",
//                    "http://www.lindaikejisblog.com/2016/04/manny-pacquiao-beefs-up-security-after.html",
//                    "http://www.lindaikejisblog.com/2016/04/former-nollywood-actress-anita-hogan.html",
//                    "http://www.lindaikejisblog.com/2015/06/dear-lib-readers-my-wife-complains-that.html",
//                    "http://www.lindaikejisblog.com/2015/06/photos-femi-otedolas-daughter-graduates.html",
//                    "http://www.lindaikejisblog.com/2015/06/former-miss-mississippis-boobs-rots.html"
                };
                break;
            case "naij":
                urls = new String[]{
                    "https://www.naij.com/1054108-awww-officer-civilian-fall-love-photos.html",
                    "https://www.naij.com/812314-shocking-efcc-uncovers-12-9-billion-arms-deal-fraud.html",
                    "https://www.naij.com/460524-read-happened-men-trekked-atiku.html",
                    "https://www.naij.com/460495-live-ngr-vs-chad-afcon-qualifier.html",
                    "https://www.naij.com/460491-photos-dprince-is-now-a-father.html"
                };
                break;
            case "dailytrust":
                urls = new String[]{
                    "http://www.dailytrust.com.ng/news/general/polio-fg-to-include-govs-in-task-force/130064.html",
                    "http://www.dailytrust.com.ng/news/general/metuh-s-handcuffs-in-order-oshiomhole/130057.html",
                    "http://www.dailytrust.com.ng/news/politics/2019-inec-to-deploy-new-tech-for-collation/130017.html"};
                break;
            case "ngrguardiannews":
                urls = new String[]{
                    "http://www.ngrguardiannews.com/2015/06/malaysian-villagers-beg-spirits-to-end-quake-aftershocks/",
                    "http://www.ngrguardiannews.com/2015/06/singapore-gay-rights-rally-draws-record-crowd-organisers/",
                    "http://www.ngrguardiannews.com/2015/06/new-york-rally-launches-clintons-bid-for-white-house/"
                };
                break;
            case "punchng":
                urls = new String[]{
                    "http://www.punchng.com/bizarre-social-media-craze-accident-victims-groan-as-sympathisers-record-agony-on-smart-phones/",
                    "http://www.punchng.com/rivers-rerun-police-confirm-one-death-12-arrests/"
                };
                break;
            case "saharareporters":
                urls = new String[]{
                    "http://saharareporters.com/2015/06/15/exclusive-garba-shehu-speaks-saharatv-explains-delayed-ministerial-appointments",
                    "http://saharareporters.com/2015/06/15/serap-icc-should-refer-south-africa-un-security-council-refusing-arrest-al-bashir",
                    "http://saharareporters.com/2015/06/15/us-commits-5-billion-military-assistance-against-boko-haram"
                };
                break;
            case "sunnewsonline":
                urls = new String[]{
                    "http://sunnewsonline.com/%e2%80%8ecbn-gov-emefiele-hails-buhari-security-agencies-over-wifes-release/",
                    "http://sunnewsonline.com/trained-in-niger-delta-creeks-returned-to-kogi-set-up-robbery-gang/",
                    "http://sunnewsonline.com/risaa-reads-riot-act-to-advertisers-orders-removal-of-illegal-structures/"
                };
                break;
//            case "sunnewsonline_breaking":
//                urls = new String[]{
//                    "http://sunnewsonline.com/trained-in-niger-delta-creeks-returned-to-kogi-set-up-robbery-gang/",
//                    "http://sunnewsonline.com/risaa-reads-riot-act-to-advertisers-orders-removal-of-illegal-structures/"
//                    "http://sunnewsonline.com/new/?p=123586",
//                    "http://sunnewsonline.com/new/?p=123571",
//                    "http://sunnewsonline.com/new/?p=123565"
//                };
//                break;
//            case "sunnewsonline_national":
//                urls = new String[]{
//                    "http://sunnewsonline.com/new/?p=123586",
//                    "http://sunnewsonline.com/new/?p=123571",
//                    "http://sunnewsonline.com/new/?p=123565"
//                };
//                break;
            case "thenationonlineng":    
                urls = new String[]{
                    "http://thenationonlineng.net/new/first-lady-dont-pay-any-money-to-see-president/",
                    "http://thenationonlineng.net/new/bpe-in-n1-45b-scam/",
                    "http://thenationonlineng.net/new/buhari-considers-balance-in-sgf-choice/"
                };
                break;
            case "thisday":
                urls = new String[]{
                      "http://www.thisdaylive.com/index.php/2016/10/22/heineken-lagos-fashion-and-design-week-cocktail-party/",
                      "http://www.thisdaylive.com/index.php/2016/10/20/of-aso-rock-demons-and-the-other-room/",
                      "http://www.thisdaylive.com/index.php/2016/10/22/aisha-buhari-not-against-her-husband-says-apc-chieftain/",
                      "http://www.thisdaylive.com/index.php/2016/10/22/buharis-daughter-denies-bbog-franchise-infringement/"
//                    "http://www.thisdaylive.com/articles/nerc-to-revoke-inoperative-power-generation-licences/212109/",
//                    "http://www.thisdaylive.com/articles/ssanu-ask-buhari-to-sack-nuc-executive-secretary/212108/",
//                    "http://www.thisdaylive.com/articles/i-have-no-interest-in-sgf-position-says-oyegun/212106/"
                };
                break;
            case "channelstv_headlines":
                urls = new String[]{
                    "https://www.channelstv.com/2017/09/29/judge-orders-dss-to-produce-dasuki-in-court/"
//                    "http://www.channelstv.com/2015/10/21/tribunal-adjourns-trial-of-bukola-saraki-till-november-5/",
//                    "http://www.channelstv.com/2015/10/21/reps-to-investigate-nnpc-joint-venture-operations/",
//                    "http://www.channelstv.com/2015/10/19/buhari-meets-with-service-chiefs-gets-assurance-of-peace-in-north-east/"
                };
                break;
            case "aitonline_news":
                urls = new String[]{
                    "http://www.aitonline.tv/post-council_of_state_approves_president___s_nomination_of_new_inec_chairman__5_national_commissioners",
                    "http://www.aitonline.tv/post-lagos_state_govt_donates_n150m_to_adamawa__yobe_and_borno",
                    "http://www.aitonline.tv/post-don_t_compare_me_with_ronaldo_and_messi___lewandowski"
                };
                break;
            case "vanguardngr":
                urls = new String[]{
                    "http://www.vanguardngr.com/2016/09/masaris-mistakes-kachikwus-naivety/",
                    "http://www.vanguardngr.com/2016/07/s-paul-a-false-christ/",
                    "http://www.vanguardngr.com/2016/09/mama-peace-billions/",
                    "http://www.vanguardngr.com/2016/06/whose-report-believe/"
                };
                break;
            case "thenewsminute":
                urls = new String[]{
                    "http://www.thenewsminute.com/article/why-bystanders-tend-look-other-way-when-people-get-attacked-50369",
                    "http://www.thenewsminute.com/article/comedian-raju-srivastav-cancels-show-pakistan-50375",
                    "http://www.thenewsminute.com/article/thought-provoking-short-film-child-sexual-abuse-depicts-untold-agony-victims-50260",
                    "http://www.thenewsminute.com/article/his-directorial-debut-power-pandi-dhanush-play-cameo-50356"
                };
                break;
            default:    
                urls = null;
        }        
        return urls;
    }

    public void log(Class aClass, String msg) {
System.out.println(LocalDateTime.now() +". "+ (aClass==null?"":aClass.getSimpleName())+"--- "+msg);        
    }
}
