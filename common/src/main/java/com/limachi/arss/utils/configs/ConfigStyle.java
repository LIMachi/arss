package com.limachi.arss.utils.configs;

import java.util.function.Function;
import java.util.regex.Pattern;

public class ConfigStyle {
    public static final ConfigStyle DEFAULT = new ConfigStyle();
    public static final ConfigStyle TOP = new ConfigStyle(){
        @Override
        public void setOverride() {
            mapStarter = "";
            mapFinisher = "";
            pairSeparator = "=";
            listStarter = "";
            listFinisher = "";
            separator = "\n";
            spacer = "";
        }

        @Override
        public String transformer(String output, boolean isKey, ConfigValue original) {
            if (isKey && original.type == ConfigValueType.STRING)
                output = output.replaceAll("^" + stringDelimiter, "").replaceAll(stringDelimiter + "$", "");
            return output;
        }
    };
    public static final Function<Integer, ConfigStyle> DEFAULT_PROVIDER = d->d == 0 ? TOP : DEFAULT;
    public String nullToken = "";
    public final Pattern validNullToken = Pattern.compile("|NULL");
    public String newLine = "\n";
    public final Pattern validNewLines = Pattern.compile("\n?\r?|\r?\n?");
    public String spacer = " ";
    public final Pattern validSpacer = Pattern.compile("[\t ]*");
    public String depthSpacer = "    ";
    public final Pattern validDepthSpacer = validSpacer;
    public String commentStarter = "#";
    public final Pattern validCommentStarter = Pattern.compile("[#?@;]|//");
    public String stringDelimiter = "\"";
    public final Pattern validStringDelimiter = Pattern.compile("[\"']");
    public String pairSeparator = ":";
    public final Pattern validPairSeparator = Pattern.compile("[:=]|[=-]?>|<[=-]?");
    public String mapStarter = "{";
    public final Pattern validMapStarter = Pattern.compile("[{(\\[]?");
    public String mapFinisher = "}";
    public final Pattern validMapFinisher = Pattern.compile("[})]]?");
    public String listStarter = "[";
    public final Pattern validListStarter = validMapStarter;
    public String listFinisher = "]";
    public final Pattern validListFinisher = validMapFinisher;
    public String separator = ",";
    public final Pattern validSeparator = Pattern.compile("[,-]");
    public String booleanTrue = "TRUE";
    public final Pattern validBooleanTrue = Pattern.compile("[Tt](:?[Rr][Uu][Ee])?|[Oo][Kk]|[Yy](:?[Ee][Ss])?");
    public String booleanFalse = "FALSE";
    public final Pattern validBooleanFalse = Pattern.compile("[Ff](:?[Aa][Ll][Ss][Ee])?|[Nn][Oo]?");
    public boolean compact = false; //if true, do not use spacer
    public int preferredWidth = 100;
    public ConfigStyle() { setOverride(); }
    public void setOverride() {}
    public String transformer(String output, boolean isKey, ConfigValue original) {
        return output;
    }
}
