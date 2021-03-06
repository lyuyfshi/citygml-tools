/*
 * citygml-tools - Collection of tools for processing CityGML files
 * https://github.com/citygml4j/citygml-tools
 *
 * citygml-tools is part of the citygml4j project
 *
 * Copyright 2018-2020 Claus Nagel <claus.nagel@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citygml4j.tools.option;

import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.cityjson.CityJSONBuilder;
import org.citygml4j.builder.cityjson.CityJSONBuilderException;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONInputFactory;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONReadException;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONReader;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.module.Modules;
import org.citygml4j.tools.util.ObjectRegistry;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InputOptions {
    @CommandLine.Parameters(paramLabel = "<file>", description = "File(s) or directory to process (glob patterns allowed).")
    private String file;

    @CommandLine.Option(names = "--input-encoding", description = "Encoding of input file(s).")
    private String encoding;

    public String getFile() {
        return file;
    }

    public String getEncoding() {
        return encoding;
    }

    public CityGMLInputFactory createCityGMLInputFactory(boolean useChunks) throws CityGMLBuilderException {
        CityGMLBuilder builder = ObjectRegistry.getInstance().get(CityGMLBuilder.class);
        CityGMLInputFactory in = builder.createCityGMLInputFactory();
        in.setProperty(CityGMLInputFactory.SKIP_GENERIC_ADE_CONTENT, true);

        if (useChunks)
            in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);

        return in;
    }

    public CityGMLReader createCityGMLReader(Path inputFile, boolean useChunks) throws CityGMLBuilderException, CityGMLReadException {
        CityGMLInputFactory in = createCityGMLInputFactory(useChunks);
        return encoding == null ?
                in.createCityGMLReader(inputFile.toFile()) :
                in.createCityGMLReader(inputFile.toFile(), encoding);
    }

    public CityGMLReader createCityGMLReader(Path inputFile, CityGMLInputFilter filter) throws CityGMLBuilderException, CityGMLReadException {
        CityGMLInputFactory in = createCityGMLInputFactory(true);
        CityGMLReader reader = encoding == null ?
                in.createCityGMLReader(inputFile.toFile()) :
                in.createCityGMLReader(inputFile.toFile(), encoding);

        return in.createFilteredCityGMLReader(reader, filter);
    }

    public CityGMLInputFilter createSkipFilter(String... localNames) {
        Set<String> filter = new HashSet<>(Arrays.asList(localNames));
        return name -> !filter.contains(name.getLocalPart())
                || !Modules.isCityGMLModuleNamespace(name.getNamespaceURI());
    }

    public CityJSONInputFactory createCityJSONInputFactory(boolean processUnknownExtensions) throws CityJSONBuilderException {
        CityJSONBuilder builder = CityGMLContext.getInstance().createCityJSONBuilder();
        CityJSONInputFactory in = builder.createCityJSONInputFactory();
        in.setProcessUnknownExtensions(processUnknownExtensions);

        return in;
    }

    public CityJSONReader createCityJSONReader(Path inputFile, boolean processUnknownExtensions) throws CityJSONBuilderException, CityJSONReadException {
        CityJSONInputFactory in = createCityJSONInputFactory(processUnknownExtensions);
        return encoding == null ?
                in.createCityJSONReader(inputFile.toFile()) :
                in.createCityJSONReader(inputFile.toFile(), encoding);
    }
}
