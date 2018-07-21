/*
 * Copyright 2018 NUROX Ltd.
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
package com.idisc.core.extraction.metaselector;

import com.bc.meta.selector.util.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.Map;
import org.json.simple.parser.JSONParser;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 20, 2018 9:42:12 PM
 */
public class JsonParserImpl implements JsonParser {

    @Override
    public Map parse(Reader reader) throws IOException, ParseException {
        try{
            return (Map)new JSONParser().parse(reader);
        }catch(org.json.simple.parser.ParseException e) {
            throw new ParseException(e.getLocalizedMessage(), e.getPosition());
        }
    }
}
