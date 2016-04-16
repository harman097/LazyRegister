package com.brent.harman.lazyregister;

import android.content.res.Resources;

import com.brent.harman.lazyregister.DrawerData.CashDrawer;
import com.brent.harman.lazyregister.DrawerData.CashDrawerConfiguration;
import com.brent.harman.lazyregister.DrawerData.Currency;
import com.brent.harman.lazyregister.DrawerData.DrawerCleaner;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Brent on 5/28/15.
 *
 * Yes, I eschewed using a database.  I really wanted to, for the experience, but it seemed like
 * too much unnecessary work - and working with serializable was good exp too
 */
public class SavedDataHandler {
    private static class SingletonHelper{
        private static final SavedDataHandler singleInstance = new SavedDataHandler();
    }

    public static SavedDataHandler getInstance() { return SingletonHelper.singleInstance; }

    private static final String TAG = "SavedDataHandler";
    private static final String CLEANER_FILE = "DrawerCleaners.dat";
    private static final String CONFIG_FILE = "DrawerConfigs.dat";
    private static final String DRAWER_FILE = "Drawers.dat";
    private static final int MAX_SAVED_DRAWERS = 25;
    private File saveDir;
    private Resources mResources;
    private Currency.CurrencyType defaultCurrencyType; // this is the working AND default
    private final List<CashDrawer> drawerList = new LinkedList<>();
    private final HashMap<Currency.CurrencyType, List<DrawerCleaner>> drawerCleanerLists = new HashMap<>();
    private final HashMap<Currency.CurrencyType, List<CashDrawerConfiguration>> drawerConfigLists = new HashMap<>();
    private final HashMap<Currency.CurrencyType, CashDrawerConfiguration> defaultConfigForCurrency = new HashMap<>();
    private final HashMap<Currency.CurrencyType, DrawerCleaner> defaultCleanerForCurrency = new HashMap<>();


    private SavedDataHandler(){}

    private void init(){
        // This will get changed if the last edited drawer was of a different currency
        defaultCurrencyType = Currency.CurrencyType.USD;

        tryLoadDrawerCleaners();
        tryLoadDrawerConfigs();
        if(!tryLoadDrawers()){
            CashDrawer newDrawer = new CashDrawer(getDefaultDrawerConfigurationFor(defaultCurrencyType));
            drawerList.add(newDrawer);
        }

        // Each drawer will save its config since this is easier to do than custom serializing
        // each drawer or bothering to do this with a database instead
        consolidateDrawerConfigReferences();
    }

    public File getSaveDir() { return saveDir; }

    public synchronized boolean initFrom(File saveDir, Resources resources) {
        if(hasBeenInitialized()) return false;
        this.mResources = resources;
        this.saveDir = saveDir;
        init();
        return true;
    }

    public boolean hasBeenInitialized(){ return saveDir != null; }

    public List<CashDrawer> getDrawerList() { return drawerList; }

    // Default and working currency types are one and the same for the foreseeable future
    public Currency.CurrencyType getDefaultCurrencyType() { return defaultCurrencyType; }
    public Currency.CurrencyType getWorkingCurrencyType() { return defaultCurrencyType; }

    public List<CashDrawerConfiguration> getDrawerConfigListFor(Currency.CurrencyType cType){
        if(!drawerConfigLists.containsKey(cType)) generateDefaultDrawerConfigurationFor(cType);
        return drawerConfigLists.get(cType);
    }

    public CashDrawerConfiguration getDefaultDrawerConfigurationFor(Currency.CurrencyType cType){
        if(!defaultConfigForCurrency.containsKey(cType)) generateDefaultDrawerConfigurationFor(cType);
        return defaultConfigForCurrency.get(cType);
    }

    private CashDrawerConfiguration generateDefaultDrawerConfigurationFor(Currency.CurrencyType cType){
        CashDrawerConfiguration newConfig = new CashDrawerConfiguration(
                cType,
                null, // binsPerRow array (if null it defaults)
                false // if true, it leaves bins empty
        );
        newConfig.setName(mResources.getString(R.string.basic_DEFAULT_LAYOUT));
        defaultConfigForCurrency.put(cType, newConfig);
        if(!drawerConfigLists.containsKey(cType)) drawerConfigLists.put(cType, new ArrayList<CashDrawerConfiguration>(1));
        drawerConfigLists.get(cType).add(newConfig);
        return newConfig;
    }

    public List<DrawerCleaner> getDrawerCleanerListFor(Currency.CurrencyType cType) {
        if(!drawerCleanerLists.containsKey(cType)) generateDefaultDrawerCleanerFor(cType);
        return drawerCleanerLists.get(cType);
    }

    public DrawerCleaner getDefaultDrawerCleanerFor(Currency.CurrencyType cType){
        if(!defaultCleanerForCurrency.containsKey(cType)) generateDefaultDrawerCleanerFor(cType);
        return defaultCleanerForCurrency.get(cType);
    }

    private DrawerCleaner generateDefaultDrawerCleanerFor(Currency.CurrencyType cType){
        DrawerCleaner newCleaner = new DrawerCleaner(cType);
        defaultCleanerForCurrency.put(cType, newCleaner);
        if(!drawerCleanerLists.containsKey(cType)) drawerCleanerLists.put(cType, new ArrayList<DrawerCleaner>(1));
        drawerCleanerLists.get(cType).add(newCleaner);
        newCleaner.setName(mResources.getString(R.string.DefaultDrawerCleanerName));
        return newCleaner;
    }

    public boolean hasSavedDrawers(){ return !drawerList.isEmpty(); }

    public CashDrawer getLastEditedDrawer() {
        if(drawerList.isEmpty()) return null;
        return drawerList.get(0);
    }

    public CashDrawer getNewDrawer(){
        CashDrawer newDrawer = new CashDrawer(getDefaultDrawerConfigurationFor(defaultCurrencyType));
        drawerList.add(0, newDrawer);
        return newDrawer;
    }

    public void setDefaultCurrencyType(Currency.CurrencyType defaultCurrencyType) {
        this.defaultCurrencyType = defaultCurrencyType;
    }

    public void addNewConfig(CashDrawerConfiguration cdc, boolean makeDefaultForCurrency){
        if(cdc == null) return;
        List<CashDrawerConfiguration> cdcList = drawerConfigLists.get(cdc.getCurrency().getCurrencyType());
        if(cdcList == null) return;

        cdcList.add(cdc);
        if(makeDefaultForCurrency){
            defaultConfigForCurrency.put(cdc.getCurrency().getCurrencyType(), cdc);
        }
    }

    public void addNewCleaner(DrawerCleaner dc, boolean makeDefaultForCurrency){
        if(dc == null) return;
        List<DrawerCleaner> dcList = drawerCleanerLists.get(dc.getCurrency().getCurrencyType());
        // There should always be a default cleaner made for a new currency, so this list should always
        // exist within the hashmap - if dcList == null, somethin' up and misbehaved.  In my brain.
        if(dcList == null) return;

        dcList.add(dc);
        if(makeDefaultForCurrency){
            defaultCleanerForCurrency.put(dc.getCurrency().getCurrencyType(), dc);
        }
    }

    public boolean renameConfig(String originalName, String newName){
        List<CashDrawerConfiguration> cdcList = drawerConfigLists.get(defaultCurrencyType);
        for(CashDrawerConfiguration cdc : cdcList){
            if(cdc.getName().equals(originalName)){
                cdc.setName(newName);
                saveConfig(cdc);
                return true;
            }
        }
        return false;
    }

    public CashDrawerConfiguration getConfig(String name){
        for(CashDrawerConfiguration cdc : drawerConfigLists.get(defaultCurrencyType)){
            if(cdc.getName().equals(name)) return cdc;
        }
        return null;
    }

    private boolean tryLoadDrawerCleaners(){
        if(saveDir==null) return false;
        File[] saveFiles = saveDir.listFiles();

        File cleanerSaveFile = null;
        for(File saveFile : saveFiles){
            if(saveFile.getName().equals(CLEANER_FILE)){
                cleanerSaveFile = saveFile;
                break;
            }
        }
        if(cleanerSaveFile == null) return false;

        InputStream file, buffer;
        ObjectInput input;

        try {
            file = new FileInputStream(cleanerSaveFile);
            buffer = new BufferedInputStream(file);
            input = new ObjectInputStream(buffer);

            int numLists = input.readInt();
            int numCleaners;
            DrawerCleaner dc;
            for(int i = 0; i < numLists; i++){
                numCleaners = input.readInt();
                for(int j = 0; j < numCleaners; j++) {
                    dc = (DrawerCleaner) input.readObject();
                    if (!drawerCleanerLists.containsKey(dc.getCurrency().getCurrencyType())) {
                        drawerCleanerLists.put(dc.getCurrency().getCurrencyType(), new LinkedList<DrawerCleaner>());
                    }
                    drawerCleanerLists.get(dc.getCurrency().getCurrencyType()).add(dc);
                    // We want the last created one to be default and there likely will be only one
                    // of these ever created for most people - so just keep overwriting it as we go
                    defaultCleanerForCurrency.put(dc.getCurrency().getCurrencyType(), dc);
                }
            }
        } catch (ClassNotFoundException e) {
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform input: " + e.toString());
            cleanerSaveFile.delete();
            drawerCleanerLists.clear();
            return false;
        } catch (IOException e) {
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform input: " + e.toString());
            cleanerSaveFile.delete();
            drawerCleanerLists.clear();
            return false;
        }
        return true;
    }

    private boolean tryLoadDrawerConfigs(){
        if(saveDir==null) return false;
        File[] saveFiles = saveDir.listFiles();

        File configSaveFile = null;
        for(File saveFile : saveFiles){
            if(saveFile.getName().equals(CONFIG_FILE)){
                configSaveFile = saveFile;
                break;
            }
        }
        if(configSaveFile == null) return false;

        InputStream file, buffer;
        ObjectInput input;

        try {
            file = new FileInputStream(configSaveFile);
            buffer = new BufferedInputStream(file);
            input = new ObjectInputStream(buffer);

            int numLists = input.readInt();
            int numConfigs;
            CashDrawerConfiguration cdc;
            for(int i = 0; i < numLists; i++){
                numConfigs = input.readInt();
                for(int j = 0; j < numConfigs; j++) {
                    cdc = (CashDrawerConfiguration) input.readObject();
                    if (!drawerConfigLists.containsKey(cdc.getCurrency().getCurrencyType())) {
                        drawerConfigLists.put(cdc.getCurrency().getCurrencyType(), new LinkedList<CashDrawerConfiguration>());
                    }
                    drawerConfigLists.get(cdc.getCurrency().getCurrencyType()).add(cdc);
                }
            }
        } catch (ClassNotFoundException e) {
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform input (ClassNotFound) : " + e.toString());
            configSaveFile.delete();
            drawerConfigLists.clear();
            return false;
        } catch (IOException e) {
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform input (IOException) : " + e.toString());
            configSaveFile.delete();
            drawerConfigLists.clear();
            return false;
        }
        return true;
    }

    private boolean tryLoadDrawers(){
        if(saveDir==null) return false;
        File[] saveFiles = saveDir.listFiles();

        File drawerSaveFile = null;
        for(File saveFile : saveFiles){
            if(saveFile.getName().equals(DRAWER_FILE)){
                drawerSaveFile = saveFile;
                break;
            }
        }
        if(drawerSaveFile == null) return false;

        InputStream file, buffer;
        ObjectInput input;
        try {
            file = new FileInputStream(drawerSaveFile);
            buffer = new BufferedInputStream(file);
            input = new ObjectInputStream(buffer);

            int numDrawers = input.readInt();
            CashDrawer d;
            for(int i = 0; i < numDrawers; i++){
                d = (CashDrawer) input.readObject();
                drawerList.add(d);
            }
        } catch (ClassNotFoundException e) {
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform input: " + e.toString());
            drawerSaveFile.delete();
            drawerList.clear();
            return false;
        } catch (IOException e) {
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform input: " + e.toString());
            drawerSaveFile.delete();
            drawerList.clear();
            return false;
        }


        if(drawerList.size() > 1){
            Collections.sort(drawerList, new Comparator<CashDrawer>() {
                @Override
                public int compare(CashDrawer lhs, CashDrawer rhs) {
                    if (lhs.getFirstEditDate() == null) return 1;
                    if (rhs.getFirstEditDate() == null) return -1;
                    if (rhs.getFirstEditDate().after(lhs.getFirstEditDate())) return 1;
                    else return -1;
                }
            });
        }
        return true;
    }

    // With this implementation, we're just going to keep them all in one file and, therefore,
    // save all of them any time I happen to request that one be saved - again, tiny amounts
    // of data we're dealing with here and I might want to change this at some point to be more
    // legitimate - so I'll keep these functions for now
    public void saveDrawer(CashDrawer d){ saveDrawers(); }
    public void saveConfig(CashDrawerConfiguration cdc){ saveConfigs(); }
    public void saveCleaner(DrawerCleaner dc){ saveCleaners(); }

    public void saveDrawers(){
        if(saveDir == null) return;
        try{
            File newSave = new File(saveDir, DRAWER_FILE);
            OutputStream file = new FileOutputStream(newSave);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            if(drawerList.size() > MAX_SAVED_DRAWERS){
                output.writeInt(MAX_SAVED_DRAWERS);
                for(int i = 0; i < MAX_SAVED_DRAWERS; i++){
                    output.writeObject(drawerList.get(i));
                }
            } else {
                output.writeInt(drawerList.size());
                for (CashDrawer d : drawerList) {
                    output.writeObject(d);
                }
            }
            output.close();
            file.close();
        }
        catch(IOException e){
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform output: "+e.toString());
        }
    }
    public void saveConfigs(){
        if(saveDir == null) return;
        try{
            File newSave = new File(saveDir, CONFIG_FILE);
            OutputStream file = new FileOutputStream(newSave);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            List<CashDrawerConfiguration> cdcList;
            output.writeInt(drawerConfigLists.size());
            for(HashMap.Entry<Currency.CurrencyType, List<CashDrawerConfiguration>> entry :
                    drawerConfigLists.entrySet()){
                cdcList = entry.getValue();
                output.writeInt(cdcList.size());
                for(CashDrawerConfiguration cdc : cdcList) output.writeObject(cdc);
            }
            output.close();
            file.close();
        }
        catch(IOException e){
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform output: "+e.toString());
        }
    }
    public void saveCleaners(){
        if(saveDir == null) return;
        try{
            File newSave = new File(saveDir, CLEANER_FILE);
            OutputStream file = new FileOutputStream(newSave);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            List<DrawerCleaner> dcList;
            output.writeInt(drawerCleanerLists.size());
            for(HashMap.Entry<Currency.CurrencyType, List<DrawerCleaner>> entry :
                    drawerCleanerLists.entrySet()){
                dcList = entry.getValue();
                output.writeInt(dcList.size());
                for(DrawerCleaner dc : dcList) output.writeObject(dc);
            }
            output.close();
            file.close();
        }
        catch(IOException e){
            //OFF FOR RELEASE Log.d(TAG, "Cannot perform output: "+e.toString());
        }
    }

    public void deleteCleaner(DrawerCleaner dc){
        drawerCleanerLists.get(dc.getCurrency().getCurrencyType()).remove(dc);
        saveCleaners();
    }

    public void deleteDrawer(CashDrawer d){
        drawerList.remove(d);
        saveDrawers();
    }

    public boolean deleteConfig(CashDrawerConfiguration cdcToDelete){
        Currency.CurrencyType cType = cdcToDelete.getCurrency().getCurrencyType();
        // If this is the only item in the list or the cdcToDelete isn't even in the list,
        // then just return false (not successful)
        if(drawerConfigLists.get(cType).size() <= 1 ||
                !drawerConfigLists.get(cType).contains(cdcToDelete)) return false;

        // Otherwise remove it
        drawerConfigLists.get(cType).remove(cdcToDelete);

        // Replace the default if necessary
        CashDrawerConfiguration cdcToReplaceWith = null;
        if(defaultConfigForCurrency.get(cType) == cdcToDelete){
            // Look for one with the original Default name to use first
            String defaultTitle = mResources.getString(R.string.basic_DEFAULT_LAYOUT);
            for(CashDrawerConfiguration cdc : drawerConfigLists.get(cType)){
                if(cdc.getName().equals(defaultTitle)){
                    cdcToReplaceWith = cdc;
                    defaultConfigForCurrency.put(cType, cdc);
                    break;
                }
            }
            // If there wasn't one (it was renamed or deleted), just use the first item
            // in the list
            if(cdcToReplaceWith == null) {
                cdcToReplaceWith = drawerConfigLists.get(cType).get(0);
                defaultConfigForCurrency.put(cType, cdcToReplaceWith);
            }
        }
        else cdcToReplaceWith = defaultConfigForCurrency.get(cType);

        // Search for instances of the one we're deleting among the drawers and replace them
        for(CashDrawer cd : drawerList){
            if(cd.getBinConfiguration() == cdcToDelete){
                cd.setBinConfiguration(cdcToReplaceWith);
            }
        }
        saveDrawers();
        saveConfigs();
        return true;
    }

    private void consolidateDrawerConfigReferences(){
        if(drawerList.isEmpty()) return;
        List<CashDrawerConfiguration> curList;
        Currency.CurrencyType cType;
        HashMap<Currency.CurrencyType, Date> dateOfLastUsedConfigFor = new HashMap<>();
        boolean configWithSameNameFound;
        for(CashDrawer d : drawerList){
            configWithSameNameFound = false;
            cType = d.getCurrency().getCurrencyType();
            curList = getDrawerConfigListFor(cType);

            // If it has a name, search for a config with the same name and replace the instance
            if(d.getBinConfiguration().hasName()){
                for(CashDrawerConfiguration preExistingConfig : curList){
                    if(d.getBinConfiguration().getName().equals(preExistingConfig.getName())){
                        d.setBinConfiguration(preExistingConfig);
                        configWithSameNameFound = true;
                        break;
                    }
                }

            }

            // If it wasn't flagged above, it needs to be added to the list
            if(!configWithSameNameFound){
                curList.add(d.getBinConfiguration());
                if(!d.getBinConfiguration().hasName())
                    d.getBinConfiguration().setName(mResources.getString(R.string.basic_Layout) +
                            " " + String.valueOf(curList.size()));
            }

            // if this is the only custom one for this currency -OR- its been used more
            // recently than any other we just added, make it the default and note its date
            if(!dateOfLastUsedConfigFor.containsKey(cType) ||
                    d.getCreationDate().after(dateOfLastUsedConfigFor.get(cType))){
                defaultConfigForCurrency.put(cType, d.getBinConfiguration());
                dateOfLastUsedConfigFor.put(cType, d.getCreationDate());
            }
        }
    }
}
