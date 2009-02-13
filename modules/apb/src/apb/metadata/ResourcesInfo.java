
// Copyright 2008-2009 Emilio Lopez-Gabeiras
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License

package apb.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//
// User: emilio
// Date: Oct 1, 2008
// Time: 5:02:29 PM

//
public class ResourcesInfo
{
    //~ Instance fields ......................................................................................

    /**
     * The directory for the resources.
     * The default is the source directory for the module.
     */
    @BuildProperty public String dir = "$source";
    /**
     * The target directory for the resources.
     * The default is the output directory for the module.
     */
    @BuildProperty public String output = "$output";

    /**
     * The encoding used when filtering resources.
     */
    @BuildProperty public String encoding = DEFAULT_ENCODING;

    /**
     * Wheter to use filtering or not.
     */
    @BuildProperty public boolean filtering = false;

    /**
     * File extensions to not apply filtering (The following extensions are always filtered: jpg, jpeg, gif, bmp, png)
     */
    private final List<String> doNotFilter = new ArrayList<String>(DEFAULT_DO_NOT_FILTER);

    /**
     * The list of files to exclude.
     */
    private final List<String> excludes = new ArrayList<String>();

    /**
     * The list of files to include.
     * If includes is empty everything will be included except Java Files.
     */
    private final List<String> includes = new ArrayList<String>();

    //~ Methods ..............................................................................................

    public List<String> doNotFilter()
    {
        return doNotFilter;
    }

    public List<String> includes()
    {
        return includes;
    }

    public List<String> excludes()
    {
        return excludes;
    }

    /**
     * Method used to set includes to define the list of resources to be copied
     * @param patterns The list of patterns to be included
     */
    public void includes(String... patterns)
    {
        includes.addAll(Arrays.asList(patterns));
    }

    /**
     * Method used to set excludes to define the list of resources to be copied
     * @param patterns The list of patterns to be excluded
     */
    public void excludes(String... patterns)
    {
        excludes.addAll(Arrays.asList(patterns));
    }

    /**
     * Method used to add extensions to the list of non filtered ones
     * @param extensions The list of extensions to be excluded from filtering
     */
    public void doNotFilter(String... extensions)
    {
        doNotFilter.addAll(Arrays.asList(extensions));
    }

    //~ Static fields/initializers ...........................................................................

    public static final List<String> DEFAULT_DO_NOT_FILTER =
        Arrays.asList("jpg", "jpeg", "gif", "bmp", "png");

    public static final String DEFAULT_ENCODING = "UTF-8";
}
