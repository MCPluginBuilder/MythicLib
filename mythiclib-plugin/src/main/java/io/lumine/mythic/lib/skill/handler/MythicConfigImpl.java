package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.skills.placeholders.PlaceholderBoolean;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.utils.serialize.Chroma;
import io.lumine.mythic.bukkit.utils.text.Text;
import io.lumine.mythic.core.config.GenericConfig;
import io.lumine.mythic.core.skills.placeholders.parsers.PlaceholderBooleanImpl;
import io.lumine.mythic.core.skills.placeholders.parsers.PlaceholderColorImpl;
import io.lumine.mythic.lib.util.lang3.NotImplementedException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class MythicConfigImpl implements GenericConfig, Cloneable, MythicConfig {
    private String configName;
    private File file;
    private ConfigurationSection fc;

    public MythicConfigImpl(String name, ConfigurationSection fc) {
        this.configName = name;
        this.file = null;
        this.fc = fc;
    }

    public void setKey(String key) {
        this.configName = key;
    }

    public String getKey() {
        return this.configName;
    }

    public String getFileName() {
        if (this.file != null) {
            return this.file.getAbsolutePath();
        } else {
            return this.configName != null ? "Nested Configuration '" + this.configName + "'" : "API";
        }
    }

    public FileConfiguration getFileConfiguration() {
        throw new NotImplementedException();
    }

    public String getNode(String field) {
        if (this.configName != null && this.configName.length() != 0) {
            return field != null && !field.isEmpty() ? this.configName + "." : this.configName;
        } else {
            return "";
        }
    }

    public void deleteNodeAndSave() {
        this.fc.set(this.getNode((String) null), (Object) null);
        this.save();
    }

    public boolean isSet(String key) {
        ConfigurationSection var10000 = this.fc;
        String var10001 = this.getNode(key);
        return var10000.isSet(var10001 + key);
    }

    public String determineWhichKeyToUse(String def, String... keys) {
        String[] var3 = keys;
        int var4 = keys.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String key = var3[var5];
            if (this.isSet(key)) {
                return key;
            }
        }

        return def;
    }

    public void set(String key, Object value) {
        this.fc.set(this.getNode(key) + key, value);
    }

    public void setSave(String field, Object value) {
        this.fc.set(this.getNode(field) + field, value);
        this.save();
    }

    public void unset(String field) {
        this.fc.set(this.getNode(field) + field, (Object) null);
    }

    public void unsetSave(String key) {
        this.unset(key);
        this.save();
    }

    public void load() {
        this.fc = YamlConfiguration.loadConfiguration(this.file);
    }

    public void save() {
        throw new NotImplementedException();
    }

    public MythicConfig getNestedConfig(String key) {
        return new MythicConfigImpl(this.getNode(key) + key, this.fc);
    }

    public Map<String, MythicConfig> getNestedConfigs(String field) {
        Map<String, MythicConfig> map = new HashMap();
        if (!this.isSet(field)) {
            return map;
        } else {
            Iterator var3 = this.getKeys(field).iterator();

            while (var3.hasNext()) {
                String k = (String) var3.next();
                map.put(k, new MythicConfigImpl(this.getNode(field) + field + "." + k, this.fc));
            }

            return map;
        }
    }

    public String getString(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.getString(key, this.fc.getString(key.toLowerCase()));
    }

    public String getString(String[] key) {
        return this.getString(key, (String) null);
    }

    public String getString(String field, String def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.getString(key, this.fc.getString(key.toLowerCase(), def));
    }

    public String getString(String[] keysToCheck, String finalDefaultValue, String... defaultValues) {
        String[] var4 = keysToCheck;
        int var5 = keysToCheck.length;

        int var6;
        String value;
        for (var6 = 0; var6 < var5; ++var6) {
            value = var4[var6];
            if (!this.isConfigurationSection(value)) {
                String get = this.getString(value, (String) null);
                if (get != null) {
                    return get;
                }
            }
        }

        var4 = defaultValues;
        var5 = defaultValues.length;

        for (var6 = 0; var6 < var5; ++var6) {
            value = var4[var6];
            if (value != null) {
                return value;
            }
        }

        return finalDefaultValue;
    }

    public Chroma getColor(String field) {
        return this.getColor(field, Chroma.of(255, 255, 255));
    }

    public Chroma getColor(String field, Chroma def) {
        String data = this.getString(field, (String) null);
        return data == null ? def : Chroma.of(data);
    }

    public PlaceholderString getPlaceholderString(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        String s = this.fc.getString(key);
        return s == null ? null : PlaceholderString.of(s);
    }

    public PlaceholderString getPlaceholderString(String field, String def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        String s = this.fc.getString(key, def);
        return s == null ? null : PlaceholderString.of(s);
    }

    public String getColorString(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        String s = this.fc.getString(key);
        if (s != null) {
            s = Text.colorizeLegacy(s);
        }

        return s;
    }

    public String getColorString(String field, String def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        String s = this.fc.getString(key, def);
        if (s != null) {
            s = Text.colorizeLegacy(s);
        }

        return s;
    }

    public boolean getBoolean(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode(field);
        return var3.getBoolean(var10001 + field);
    }

    public boolean getBoolean(String field, boolean def) {
        return this.fc.getBoolean(this.getNode(field) + field, def);
    }

    @Override
    public PlaceholderBoolean getPlaceholderBoolean(String field) {
        ConfigurationSection var10000 = this.fc;
        String var10001 = this.getNode(field);
        if (var10000.isSet(var10001 + field)) {
            var10000 = this.fc;
            var10001 = this.getNode(field);
            return PlaceholderBooleanImpl.of(var10000.getString(var10001 + field));
        } else {
            return null;
        }
    }

    @Override
    public PlaceholderBoolean getPlaceholderBoolean(String[] strings) {
        String[] var2 = strings;
        int var3 = strings.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String key = var2[var4];
            ConfigurationSection var10000 = this.fc;
            String var10001 = this.getNode(key);
            if (var10000.isSet(var10001 + key)) {
                var10000 = this.fc;
                var10001 = this.getNode(key);
                return PlaceholderBooleanImpl.of(var10000.getString(var10001 + key));
            }
        }

        return null;
    }

    @Override
    public PlaceholderBoolean getPlaceholderBoolean(String field, Boolean def) {
        ConfigurationSection var10000 = this.fc;
        String var10001 = this.getNode(field);
        if (var10000.isSet(var10001 + field)) {
            var10000 = this.fc;
            var10001 = this.getNode(field);
            return PlaceholderBooleanImpl.of(var10000.getString(var10001 + field));
        } else {
            return def == null ? null : PlaceholderBoolean.of(def.toString());
        }
    }

    @Override
    public PlaceholderBoolean getPlaceholderBoolean(String[] field, Boolean def) {
        String[] var3 = field;
        int var4 = field.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String key = var3[var5];
            ConfigurationSection var10000 = this.fc;
            String var10001 = this.getNode(key);
            if (var10000.isSet(var10001 + key)) {
                var10000 = this.fc;
                var10001 = this.getNode(key);
                return PlaceholderBooleanImpl.of(var10000.getString(var10001 + key));
            }
        }

        return def == null ? null : PlaceholderBoolean.of(def.toString());
    }

    public Boolean getBooleanNullable(String field) {
        ConfigurationSection var10000 = this.fc;
        String var10001 = this.getNode(field);
        if (var10000.isSet(var10001 + field)) {
            var10000 = this.fc;
            var10001 = this.getNode(field);
            return var10000.getBoolean(var10001 + field);
        } else {
            return null;
        }
    }

    public Boolean getBooleanNullable(String[] field) {
        String[] var2 = field;
        int var3 = field.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String key = var2[var4];
            ConfigurationSection var10000 = this.fc;
            String var10001 = this.getNode(key);
            if (var10000.isSet(var10001 + key)) {
                var10000 = this.fc;
                var10001 = this.getNode(key);
                return var10000.getBoolean(var10001 + key);
            }
        }

        return null;
    }

    public int getInteger(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.getInt(key, this.fc.getInt(key.toLowerCase()));
    }

    public int getInteger(String field, int def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.getInt(key, this.fc.getInt(key.toLowerCase(), def));
    }

    public int getInteger(String[] keys, int def) {
        String[] var3 = keys;
        int var4 = keys.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String key = var3[var5];
            String var10000 = this.getNode(key);
            key = var10000 + key;
            if (this.fc.isInt(key)) {
                return this.fc.getInt(key);
            }
        }

        return def;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public int getInt(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode(field);
        return var3.getInt(var10001 + field);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public int getInt(String field, int def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.getInt(this.getNode(field) + field, def);
    }

    public float getFloat(String field) {
        return (float) this.getDouble(field);
    }

    public float getFloat(String field, float def) {
        return (float) this.getDouble(field, (double) def);
    }

    public double getDouble(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode(field);
        return var3.getDouble(var10001 + field);
    }

    public double getDouble(String field, double def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.getDouble(this.getNode(field) + field, def);
    }

    public List<String> getStringList(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode(field);
        return var3.getStringList(var10001 + field);
    }

    public List<String> getStringList(String field, List<String> def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        ConfigurationSection var4 = this.fc;
        String var10001 = this.getNode(field);
        if (var4.isSet(var10001 + field)) {
            var4 = this.fc;
            var10001 = this.getNode(field);
            return var4.getStringList(var10001 + field);
        } else {
            return def;
        }
    }

    public List<String> getColorStringList(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        ConfigurationSection var7 = this.fc;
        String var10001 = this.getNode(field);
        List<String> list = var7.getStringList(var10001 + field);
        List<String> parsed = new ArrayList();
        if (list != null) {
            Iterator var5 = list.iterator();

            while (var5.hasNext()) {
                String str = (String) var5.next();
                parsed.add(ChatColor.translateAlternateColorCodes('&', str));
            }
        }

        return parsed;
    }

    public List<PlaceholderString> getPlaceholderStringList(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        ConfigurationSection var7 = this.fc;
        String var10001 = this.getNode(field);
        List<String> list = var7.getStringList(var10001 + field);
        List<PlaceholderString> parsed = new ArrayList();
        if (list != null) {
            Iterator var5 = list.iterator();

            while (var5.hasNext()) {
                String str = (String) var5.next();
                parsed.add(PlaceholderString.of(str));
            }
        }

        return parsed;
    }

    public List<PlaceholderString> getPlaceholderStringList(String field, List<String> def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        List<String> list = this.getStringList(field, def);
        List<PlaceholderString> parsed = new ArrayList();
        if (list != null) {
            Iterator var6 = list.iterator();

            while (var6.hasNext()) {
                String str = (String) var6.next();
                parsed.add(PlaceholderString.of(str));
            }
        }

        return parsed;
    }

    public List<PlaceholderString> getPlaceholderStringListAlt(String field, List<PlaceholderString> def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        List<String> list = this.getStringList(field);
        if (list != null && !list.isEmpty()) {
            List<PlaceholderString> parsed = new ArrayList();
            if (list != null) {
                Iterator var6 = list.iterator();

                while (var6.hasNext()) {
                    String str = (String) var6.next();
                    parsed.add(PlaceholderString.of(str));
                }
            }

            return parsed;
        } else {
            return def;
        }
    }

    public List<Map<?, ?>> getMapList(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode(field);
        return var3.getMapList(var10001 + field);
    }

    public List<?> getList(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        if (this.fc.isSet(key)) {
            return this.fc.getList(key);
        } else {
            return this.fc.isSet(key.toLowerCase()) ? this.fc.getList(key.toLowerCase()) : null;
        }
    }

    public List<Byte> getByteList(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        if (this.fc.isSet(key)) {
            return this.fc.getByteList(key);
        } else {
            return this.fc.isSet(key.toLowerCase()) ? this.fc.getByteList(key.toLowerCase()) : null;
        }
    }

    public ItemStack getItemStack(String field, String def) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        if (this.fc.isSet(key)) {
            return this.fc.getItemStack(key);
        } else if (this.fc.isSet(key.toLowerCase())) {
            return this.fc.getItemStack(key.toLowerCase());
        } else {
            try {
                return new ItemStack(Material.valueOf(def));
            } catch (Exception var5) {
                return null;
            }
        }
    }

    public boolean isConfigurationSection(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.isConfigurationSection(key);
    }

    public Set<String> getKeys() {
        return this.fc.getConfigurationSection(this.configName).getKeys(false);
    }

    public Set<String> getKeys(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.getConfigurationSection(key).getKeys(false);
    }

    public boolean isList(String field) {
        String var10000 = this.getNode(field);
        String key = var10000 + field;
        return this.fc.isList(key);
    }

    public PlaceholderInt getPlaceholderInt(String key, String def) {
        String s = this.getString(key, def);
        return s == null ? null : PlaceholderInt.of(s);
    }

    public PlaceholderInt getPlaceholderInt(String[] key, String def) {
        String s = this.getString(key, def);
        return s == null ? null : PlaceholderInt.of(s);
    }

    public PlaceholderInt getPlaceholderInt(String[] key, String def, String... args) {
        String s = this.getString(key, def, args);
        return s == null ? null : PlaceholderInt.of(s);
    }

    public PlaceholderDouble getPlaceholderDouble(String key, String def) {
        String s = this.getString(key, def);
        return s == null ? null : PlaceholderDouble.of(s);
    }

    public PlaceholderDouble getPlaceholderDouble(String[] key, String def, String... args) {
        String s = this.getString(key, def, args);
        return s == null ? null : PlaceholderDouble.of(s);
    }

    public PlaceholderColorImpl getPlaceholderColor(String key, String def) {
        PlaceholderString s = this.getPlaceholderString(key, def);
        return s == null ? null : new PlaceholderColorImpl(s);
    }

    public <T extends Enum> T getEnum(String field, Class<T> clazz, T def) {
        try {
            String in = this.getString(field);
            if (in == null) {
                return def;
            } else {
                Enum value = Enum.valueOf(clazz, in.toUpperCase());
                return value == null ? def : (T) value;
            }
        } catch (Error | Exception var6) {
            return def;
        }
    }

    public void merge(MythicConfig tmplConfig, List<String> keysToIgnore) {
        throw new NotImplementedException();
    }

    public File getFile() {
        return this.file;
    }
}
