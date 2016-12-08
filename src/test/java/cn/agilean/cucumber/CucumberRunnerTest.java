package cn.agilean.cucumber;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.cli.Main;
import cucumber.api.junit.Cucumber;

/**
 * Cucumber的运行入口。
 * <p>
 * 小心，这个工具同时支持JUNIT，和MAIN方法两种运行模式。记得选择你想要运行的模式。
 *
 * @author Alex
 *
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = CucumberRunnerTest.DIR, plugin = {"pretty", "json:target/cucumber.json"})
public class CucumberRunnerTest {

    static final String DIR = "src/test/features/";

    /**
     * <pre>
     * Usage: java cucumber.api.cli.Main [options] [[[FILE|DIR][:LINE[:LINE]*] ]+ | @FILE ]

    Options:

    -g, --glue PATH                        Where glue code (step definitions, hooks
                                         and plugins) are loaded from.
    -p, --plugin PLUGIN[:PATH_OR_URL]      Register a plugin.
                                         Built-in formatter PLUGIN types: junit,
                                         html, pretty, progress, json, usage, rerun,
                                         testng. Built-in summary PLUGIN types:
                                         default_summary, null_summary. PLUGIN can
                                         also be a fully qualified class name, allowing
                                         registration of 3rd party plugins.
    -f, --format FORMAT[:PATH_OR_URL]      Deprecated. Use --plugin instead.
    -t, --tags TAG_EXPRESSION              Only run scenarios tagged with tags matching
                                         TAG_EXPRESSION.
    -n, --name REGEXP                      Only run scenarios whose names match REGEXP.
    -d, --[no-]-dry-run                    Skip execution of glue code.
    -m, --[no-]-monochrome                 Don't colour terminal output.
    -s, --[no-]-strict                     Treat undefined and pending steps as errors.
      --snippets [underscore|camelcase]  Naming convention for generated snippets.
                                         Defaults to underscore.
    -v, --version                          Print version.
    -h, --help                             You're looking at it.
    --i18n LANG                            List keywords for in a particular language
                                         Run with "--i18n help" to see all languages

    Feature path examples:
    <path>                                 Load the files with the extension ".feature"
                                         for the directory <path>
                                         and its sub directories.
    <path>/<name>.feature                  Load the feature file <path>/<name>.feature
                                         from the file system.
    classpath:<path>/<name>.feature        Load the feature file <path>/<name>.feature
                                         from the classpath.
    <path>/<name>.feature:3:9              Load the scenarios on line 3 and line 9 in
                                         the file <path>/<name>.feature.
    &#64;<path>/<file>                         Parse <path>/<file> for feature paths generated
                                         by the rerun formatter.
     *
     * </pre>
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //args = new String[] { "--help" };
         args = new String[] {
         "-g", "cn.agilean.cucumber",
         "-p","pretty",
//         "-p","html:target/cucumber",
         "-p", "json:target/cucumber.json",
         DIR
         };
        Main.run(args, CucumberRunnerTest.class.getClassLoader());
    }
}
