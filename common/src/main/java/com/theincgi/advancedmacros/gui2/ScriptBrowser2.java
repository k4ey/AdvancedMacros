package com.theincgi.advancedmacros.gui2;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.gui.Color;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.elements.ColorTextArea;
import com.theincgi.advancedmacros.gui.elements.Drawable;
import com.theincgi.advancedmacros.gui.elements.GuiButton;
import com.theincgi.advancedmacros.gui.elements.GuiRect;
import com.theincgi.advancedmacros.gui.elements.ListManager;
import com.theincgi.advancedmacros.gui.elements.Moveable;
import com.theincgi.advancedmacros.gui.elements.OnClickHandler;
import com.theincgi.advancedmacros.gui2.PopupPrompt2.Result;
import com.theincgi.advancedmacros.gui2.PopupPrompt2.ResultHandler;
import com.theincgi.advancedmacros.misc.PropertyPalette;
import com.theincgi.advancedmacros.misc.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.Stack;

public class ScriptBrowser2 extends Gui {

    GuiButton returnButton;
    GuiButton backButton, forwardButton;
    GuiButton createFolderButton, createFileButton;
    GuiButton searchButton, pasteButton;

    GuiRect addressBackdrop;

    /**
     * Active when picking a script, not browsing
     */
    boolean selectionMode = false;

    ListManager listManager;
    ColorTextArea filePreview;

    //private PopupPrompt popupPrompt;
    PopupPrompt2 popupPrompt2;
    private PromptType promptType = null;
    private File clipboardFile = null;

    private String propAddress = "colors.scriptBrowser2";

    private File selectedFile;
    private File activePath = AdvancedMacros.MACROS_FOLDER;

    boolean cutMode;

    Stack<String> history = new Stack<>();
    private String pathText;
    public PropertyPalette propertyPalette = new PropertyPalette(new String[]{"scriptBrowser"});

    public ScriptBrowser2() {
        super();
        int defWid = 12;
        int defHei = 12;
        returnButton = new GuiButton(5, 5, defWid, defHei, Settings.getTextureID("resource:whitereturn.png"), LuaValue.NIL, "scriptBrowser", "returnButton");
        //returnButton = new GuiButton(new WidgetID(600), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitereturn.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE);
        backButton = new GuiButton(5, 5, defWid, defHei, Settings.getTextureID("resource:whiteback.png"), LuaValue.NIL, "scriptBrowser", "backButton");
        //backButton = new GuiButton(new WidgetID(601), 5, 5, defWid, defHei, Settings.getTextureID("resource:whiteback.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE);
        //forwardButton = new GuiButton(new WidgetID(602), 5, 5, defWid, defHei, Settings.getTextureID("resource:whiteforward.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE);
        forwardButton = new GuiButton(5, 5, defWid, defHei, Settings.getTextureID("resource:whiteforward.png"), LuaValue.NIL, "scriptBrowser", "forwardButton");
        //createFolderButton = new GuiButton(new WidgetID(603), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitecreatefolder.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE);
        createFolderButton = new GuiButton(5, 5, defWid, defHei, Settings.getTextureID("resource:whitecreatefolder.png"), LuaValue.NIL, "scriptBrowser", "createFolderButton");
        //		createFileButton = new GuiButton(new WidgetID(604), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitecreatefile.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE);
        createFileButton = new GuiButton(5, 5, defWid, defHei, Settings.getTextureID("resource:whitecreatefile.png"), LuaValue.NIL, "scriptBrowser", "createFileButton");
        //		searchButton = new GuiButton(new WidgetID(605), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitesearch.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE);
        searchButton = new GuiButton(5, 5, defWid, defHei, Settings.getTextureID("resource:whitesearch.png"), LuaValue.NIL, "scriptBrowser", "searchButton");
        //pasteButton = new GuiButton(new WidgetID(635), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitepaste.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE);
        pasteButton = new GuiButton(5, 5, defWid, defHei, Settings.getTextureID("resource:whitepaste.png"), LuaValue.NIL, "scriptBrowser", "pasteButton");

        //addressBackdrop = new GuiRect(new WidgetID(606), 5, 5, defWid, defHei, propAddress+".addressBackground", Color.BLACK, Color.WHITE);
        addressBackdrop = new GuiRect(5, 5, defWid, defHei, "scriptBrowser", "addressBackground");

        //		popupPrompt = new PopupPrompt(new WidgetID(404), width/3, 36, width/3, height/3,this);
        popupPrompt2 = new PopupPrompt2(this, propertyPalette);

        filePreview = new ColorTextArea(this, "browserTextPreview");
        filePreview.setEditable(false);
        filePreview.setFocused(true);

        listManager = new ListManager(5, 5, 5, 5 /*new WidgetID(607), propAddress+".list"*/, propertyPalette);

        addDrawable(returnButton);
        addDrawable(backButton);
        addDrawable(forwardButton);
        addDrawable(createFolderButton);
        addDrawable(createFileButton);
        //drawables.add(searchButton);

        addDrawable(listManager);
        addDrawable(filePreview);
        //drawables.add(popupPrompt);
        addDrawable(addressBackdrop);
        addDrawable(pasteButton);

        addInputSubscriber(returnButton);
        addInputSubscriber(backButton);
        addInputSubscriber(forwardButton);
        addInputSubscriber(createFolderButton);
        addInputSubscriber(createFileButton);
        //inputSubscribers.add(searchButton);
        addInputSubscriber(listManager);
        addInputSubscriber(filePreview);
        addInputSubscriber(pasteButton);
        //no popupPrompt, it gives itself the firstListener prop

        listManager.setModeFullBox(true);

        resize(MinecraftClient.getInstance(), width, height);

        returnButton.setOnClick((int mouseButton, GuiButton b) -> {
            EventHandler.showPrevMenu();
        });

        createFileButton.setOnClick((int mouseButton, GuiButton b) -> {
            promptType = PromptType.FILE_NAME;
            popupPrompt2.prompt("File name:");
        });
        createFolderButton.setOnClick((int mouseButton, GuiButton b) -> {
            promptType = PromptType.FOLDER_NAME;
            popupPrompt2.prompt("Folder name:");
        });

        popupPrompt2.setResultHandler((result) -> {
            if (result.canceled || promptType == null) {
                return true; //done
            }
            switch (promptType) {
                case FileAction: {
                    switch (FileActions.valueOf(result.result)) {
                        case Cut: {
                            clipboardFile = selectedFile;
                            cutMode = true;
                            return true;
                        }
                        case Copy: {
                            clipboardFile = selectedFile;
                            cutMode = false;
                            return true;
                        }
                        case DELETE: {
                            if (selectedFile != null && selectedFile.exists()) {
                                selectedFile.delete();
                            }
                            populateList(activePath);
                            return true;
                        }

                        case Rename: {
                            promptType = PromptType.RENAME;
                            popupPrompt2.prompt("Rename to:");
                            return false; //not done, keep it open
                        }
                        case Run: {
                            if (selectedFile.isFile()) {
                                AdvancedMacros.runScript(getScriptPath(selectedFile));
                            }
                            return true;
                        }
                        default:
                            break;
                    }
                    return true;
                }

                case FILE_NAME: {
                    File f = new File(activePath, result.result);
                    try {
                        f.createNewFile();
                        selectedFile = f;
                    } catch (IOException e) {
                        //TODO log to world
                        e.printStackTrace();
                    }
                    populateList(activePath);
                    return true;
                }
                case FOLDER_NAME: {
                    File f = new File(activePath, result.result);
                    f.mkdir();
                    selectedFile = f;
                    populateList(activePath);
                    return true;
                }
                case RENAME: {
                    selectedFile.renameTo(selectedFile = new File(activePath, result.result));

                    populateList(activePath);
                    return true;
                }
                default:
                    System.err.println("Unknown enum (" + promptType + ") in com.theincgi.gui2.ScriptBrowser2#ScriptBrowser2");
            }
            promptType = null;
            return true;
        });

        backButton.setEnabled(false);
        forwardButton.setEnabled(false);

        backButton.setOnClick((int mouseNum, GuiButton b) -> {
            if (canGoBackMore()) {
                history.push(activePath.getName());
                activePath = activePath.getParentFile();
                forwardButton.setEnabled(true);
                populateList(activePath);
            }
            backButton.setEnabled(canGoBackMore());
        });
        forwardButton.setOnClick((int mouseNum, GuiButton b) -> {
            if (!history.isEmpty()) {
                activePath = new File(activePath, history.pop());
                populateList(activePath);
                backButton.setEnabled(canGoBackMore());
            }
            forwardButton.setEnabled(!history.isEmpty());
        });
        pasteButton.setOnClick((int mouseNum, GuiButton b) -> {
            pasteAction();
        });
        searchButton.setOnClick((int mouseNum, GuiButton b) -> {
            popupPrompt2.showNotification("Sorry, this one\nisn't ready yet!");
        });

        listManager.setScrollSpeed(5);
        listManager.setForceFrame(true);
        listManager.setAlwaysShowScroll(true);
        listManager.setDrawBG(true);
        populateList(AdvancedMacros.MACROS_FOLDER);
        propertyPalette.addColorIfNil(Color.TEXT_f, "scriptBrowser", "colors", "file");
        propertyPalette.addColorIfNil(Color.TEXT_e, "scriptBrowser", "colors", "folder");
        propertyPalette.addColorIfNil(Color.TEXT_b, "scriptBrowser", "colors", "selectedFile");
        propertyPalette.addColorIfNil(Color.TEXT_d, "scriptBrowser", "colors", "selectedFolder");

    }

    //	private Property fileColorProp = new Property("colors.scriptBrowser2.file", Color.TEXT_f.toLuaValue(), "color.file", widgetID);
    //	private Property folderColorProp = new Property("colors.scriptBrowser2.folder", Color.TEXT_e.toLuaValue(), "color.folder", widgetID);
    //	private Property selFileColorProp = new Property("colors.scriptBrowser2.selectedFile", Color.TEXT_b.toLuaValue(), "color.selectedFile", widgetID);
    //	private Property selFolderColorProp = new Property("colors.scriptBrowser2.selectedFolder", Color.TEXT_d.toLuaValue(), "color.selectedFolder", widgetID);
    public static String getScriptPath(File filePath) {
        //System.out.println("This: ");
        try {
            String tmp = filePath.getCanonicalPath().substring(AdvancedMacros.MACROS_FOLDER.getCanonicalPath().length() + 1);
            //System.out.println(tmp+"\n"+AdvancedMacros.macrosFolder);
            return tmp;
        } catch (IOException e) {
            return filePath.toString().substring(AdvancedMacros.MACROS_FOLDER.toString().length() + 1);
        }
    }

    private boolean canGoBackMore() {
        try {
            return activePath.getParentFile().getCanonicalPath().startsWith(AdvancedMacros.MACROS_FOLDER.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    private void pasteAction() {
        if (clipboardFile == null) {
            return;
        }
        File temp = new File(activePath, clipboardFile.getName());
        {
            int i = 1;
            while (temp.exists()) {
                temp = new File(activePath, splice(clipboardFile.getName(), i));
            }
        }
        System.out.println("Copy from " + clipboardFile.toString() + " to " + temp.toString());
        try {
            if (cutMode) {
                clipboardFile.renameTo(temp);
            } else {
                Files.copy(clipboardFile.toPath(), temp.toPath(), new CopyOption[]{});
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        populateList(activePath);
    }

    private String splice(String name, int i) {
        int m = name.lastIndexOf('.');
        String a = name;
        String b = "";
        if (m >= 0) {
            a = name.substring(0, m);
            b = name.substring(m);
        }
        return a + " (" + i + ")" + b;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
        super.render(drawContext, mouseX, mouseY, partialTicks);
        drawContext.drawText(this.getFontRend(), pathText, returnButton.getX() + returnButton.getItemWidth() + 2, 8, Color.WHITE.toInt(), false);
    }

    @Override
    public void resize(MinecraftClient mc, int width, int height) {
        super.resize(mc, width, height);

        //update sizes

        returnButton.setWidth(40);
        //backButton.setWidth((int) Math.ceil(returnButton.getWid()/2f));
        //	forwardButton.setWidth((int) Math.floor(returnButton.getWid()/2f));
        createFolderButton.setWidth(73);
        createFileButton.setWidth(60);
        pasteButton.setWidth(33);

        addressBackdrop.setWidth(width - 10 - returnButton.getItemWidth());
        //addressBackdrop.setHeight(12);

        int heiAvail = height - 20 - returnButton.getItemHeight() - backButton.getItemHeight();

        listManager.setWidth(width - 10);
        listManager.setHeight((int) Math.ceil(heiAvail * 2 / 3f));

        filePreview.resize(width - 10, (int) Math.floor(heiAvail / 3f));

        //update positions

        //width/3, 36, width/3, height/3,this

        addressBackdrop.setPos(returnButton.getX() + returnButton.getItemWidth(), returnButton.getY());
        backButton.setPos(5, 5 + returnButton.getItemHeight());
        forwardButton.setPos(backButton.getX() + backButton.getItemWidth(), backButton.getY());
        createFolderButton.setPos(forwardButton.getX() + forwardButton.getItemWidth(), forwardButton.getY());
        createFileButton.setPos(createFolderButton.getX() + createFolderButton.getItemWidth(), createFolderButton.getY());
        pasteButton.setPos(createFileButton.getX() + createFileButton.getItemWidth(), createFileButton.getY());
        searchButton.setPos(width - 5 - searchButton.getItemWidth(), backButton.getY());

        listManager.setPos(5, backButton.getY() + backButton.getItemHeight() + 5);
        filePreview.setPos(5, listManager.getY() + listManager.getItemHeight() + 5);
    }

    @Override
    public Text getTitle() {
        return Text.literal("Script Browser");
    }

    public void populateList(File folder) {
        if (folder.isDirectory()) {
            try {
                pathText = activePath.getCanonicalPath().substring(AdvancedMacros.MACROS_FOLDER.getCanonicalPath().length());
            } catch (IOException e) {
                pathText = activePath.toString();
            }
            pathText = pathText.replace('\\', '/');
            if (pathText.startsWith("/")) {
                pathText = pathText.substring(1);
            }
            File[] files = folder.listFiles();
            int needed = (files.length + ROW_SIZE) / ROW_SIZE;

            if (ROW_SIZE != getColumnCount()) {
                //	System.out.println("ROW SIZE UPDATED");
                ROW_SIZE = getColumnCount();
                //				listManager.clear();
            }

            listManager.clear();
            for (int i = 0; i < needed; i++) {//doesnt have enough
                listManager.add(new FileRow());
            }

            for (int i = 0; i < files.length && i / ROW_SIZE < listManager.getItems().size(); i += ROW_SIZE) { //FIXME IndexOutOfBounds
                ((FileRow) listManager.getItem(i / ROW_SIZE)).populate(files, i);
            }
            listManager.scrollTop();
            backButton.setEnabled(canGoBackMore());
        }
    }

    //Property columnCount = new Property("scriptBrowser.columns", LuaValue.valueOf(3), "columnCount", new WidgetID(650));

    int ROW_SIZE = getColumnCount();

    public class FileRow implements Moveable, Drawable, InputSubscriber {
        //final static int SIZE = 3;

        final static int bufferSize = 5;
        float elementWidth = 0;
        int x, y;
        FileElement[] fileElements = new FileElement[ROW_SIZE];

        public FileRow() {
            for (int i = 0; i < fileElements.length; i++) {
                fileElements[i] = new FileElement();
            }
        }

        public void populate(File[] files, int offset) {
            for (int i = offset, m = 0; i < offset + fileElements.length; i++, m++) {
                if (i < files.length) {
                    fileElements[m].update(files[i]);
                } else {
                    fileElements[m].update(null);
                }
            }
        }

        @Override
        public void setPos(int x, int y) {
            this.x = x;
            this.y = y;
            for (int i = 0; i < fileElements.length; i++) {
                if (i == 0) {
                    fileElements[i].button.setPos(x, y);
                } else {
                    fileElements[i].button.setPos((int) (x + Math.floor(elementWidth + bufferSize) * i), y);
                }
            }
        }

        @Override
        public void setVisible(boolean b) {
            for (int i = 0; i < fileElements.length; i++) {
                fileElements[i].setVisible(b);
            }
        }

        @Override
        public int getItemHeight() {
            return fileElements[0].button.getItemHeight();
        }

        @Override
        public int getItemWidth() {
            return fileElements[0].button.getItemWidth() * fileElements.length + bufferSize * (fileElements.length - 1);
        }

        @Override
        public void setWidth(int i) {
            int spacing = bufferSize * (fileElements.length - 1);
            float indiv = (i - spacing) / ((float) fileElements.length);
            elementWidth = indiv;
            for (int j = 0; j < fileElements.length; j++) {
                fileElements[j].button.setWidth((int) (j == 0 ? Math.ceil(indiv) : Math.floor(indiv)));
            }
            setPos(this.x, this.y);
        }

        @Override
        public void setHeight(int i) {
            for (int j = 0; j < fileElements.length; j++) {
                fileElements[j].button.setHeight(i);
            }
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void setX(int x) {
            this.x = x;
        }

        @Override
        public void setY(int y) {
            this.y = y;
        }

        @Override
        public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
            for (int i = 0; i < fileElements.length; i++) {
                fileElements[i].button.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean onScroll(Gui gui, double i) {
            return false;
        }

        @Override
        public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
            for (int i = 0; i < fileElements.length; i++) {
                if (fileElements[i].button.onMouseClick(gui, x, y, buttonNum)) {
                    return true;
                }
            }
            ;
            return false;
        }

        @Override
        public boolean onMouseRelease(Gui gui, double x, double y, int state) {
            for (int i = 0; i < fileElements.length; i++) {
                if (fileElements[i].button.onMouseRelease(gui, x, y, state)) {
                    return true;
                }
            }
            ;
            return false;
        }

        @Override
        public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
            //System.out.println("Passing mouse Event");
            for (int i = 0; i < fileElements.length; i++) {
                if (fileElements[i].button.onMouseClickMove(gui, x, y, buttonNum, q, r)) {
                    return true;
                }
            }
            ;
            return false;
        }

        @Override
        public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean onCharTyped(Gui gui, char typedChar, int mods) {
            return false;
        }

        @Override
        public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
            return false;
        }

    }

    public class FileElement {

        //private final WidgetID widgetID = new WidgetID(610);
        //
        //		private Property fileColorProp = new Property("colors.scriptBrowser2.file", Color.TEXT_f.toLuaValue(), "color.file", widgetID);
        //		private Property folderColorProp = new Property("colors.scriptBrowser2.folder", Color.TEXT_e.toLuaValue(), "color.folder", widgetID);
        //		private Property selFileColorProp = new Property("colors.scriptBrowser2.selectedFile", Color.TEXT_b.toLuaValue(), "color.selectedFile", widgetID);
        //		private Property selFolderColorProp = new Property("colors.scriptBrowser2.selectedFolder", Color.TEXT_d.toLuaValue(), "color.selectedFolder", widgetID);
        private String selectedName;
        private String unselectedName;
        File filePath;
        GuiButton button;

        public FileElement() {
            //button = new GuiButton(widgetID, 5, 5, 12, 12, LuaValue.NIL, LuaValue.NIL,null, Color.BLACK, Color.BLACK, Color.WHITE) {
            button = new GuiButton(5, 5, 12, 12, LuaValue.NIL, LuaValue.NIL) {

                @Override
                public void onDraw(DrawContext drawContext, Gui gui, int mouseX, int mouseY, float partialTicks) {
                    if (selectedFile != null && selectedFile.equals(filePath)) {
                        if (filePath.isDirectory()) {
                            setTextColor(propertyPalette.getColor("scriptBrowser", "colors", "selectedFolder"));//Utils.parseColor(selFolderColorProp.getPropValue()));
                        } else {
                            setTextColor(propertyPalette.getColor("scriptBrowser", "colors", "selectedFile"));//Utils.parseColor(selFileColorProp.getPropValue()));
                        }
                        setText(selectedName);
                    } else {
                        if (filePath != null) {
                            if (filePath.isDirectory()) {
                                setTextColor(propertyPalette.getColor("scriptBrowser", "colors", "folder"));//Utils.parseColor(folderColorProp.getPropValue()));
                            } else {
                                setTextColor(propertyPalette.getColor("scriptBrowser", "colors", "file"));//Utils.parseColor(fileColorProp.getPropValue()));
                            }
                        }
                        setText(unselectedName);
                    }
                    super.onDraw(drawContext, gui, mouseX, mouseY, partialTicks);
                }
            };
            button.setFrame(Color.BLACK);
            button.setOnClick((int mouseButton, GuiButton b) -> {
                if (mouseButton == OnClickHandler.LMB) {
                    if (ScriptBrowser2.this.selectedFile != null && ScriptBrowser2.this.selectedFile.equals(filePath)) {
                        //TODO open file in editor
                        if (selectedFile.isDirectory()) {
                            activePath = selectedFile;
                            ScriptBrowser2.this.populateList(activePath);
                            backButton.setEnabled(true);
                            history.clear();
                            forwardButton.setEnabled(false);
                        } else {
                            if (isSelectionMode()) {
                                selectionMode = false;
                                if (requester == null) {//FIXME
                                    MinecraftClient.getInstance().setScreen(AdvancedMacros.macroMenuGui.getGui());
                                } else {
                                    MinecraftClient.getInstance().setScreen(requester);
                                }
                                Result r = new PopupPrompt2.Result();
                                r.canceled = false;
                                r.result = ScriptBrowser2.getScriptPath(selectedFile).replace('\\', '/');
                                rh.onResult(r);
                            } else {
                                EventHandler.showMenu(AdvancedMacros.editorGUI, ScriptBrowser2.this);
                                AdvancedMacros.editorGUI.openScript(getScriptPath());
                                selectedFile = null; //unselect so when you go back if you click it the file preview updates //FIXME detect return to gui
                            }
                        }
                    } else {
                        ScriptBrowser2.this.selectedFile = filePath;
                        filePreview.openScript(getScriptPath());  //FIXME Still not right, also prompt size
                        System.out.println("File selected: " + selectedFile);
                    }
                } else if (mouseButton == OnClickHandler.RMB) {
                    ScriptBrowser2.this.selectedFile = filePath;
                    //if(ScriptBrowser2.this.promptType==null) {
                    ScriptBrowser2.this.promptType = PromptType.FileAction;
                    //ScriptBrowser2.this.popupPrompt.promptChoice("Action:", FileActions.getActionList());
                    popupPrompt2.promptChoice("Action: ", FileActions.getActionList());
                    //}
                }
            });
        }

        private String getScriptPath() {
            //System.out.println("This: ");
            try {
                String tmp = filePath.getCanonicalPath().substring(AdvancedMacros.MACROS_FOLDER.getCanonicalPath().length() + 1);
                //System.out.println(tmp+"\n"+AdvancedMacros.macrosFolder);
                return tmp;
            } catch (IOException e) {
                return filePath.toString().substring(AdvancedMacros.MACROS_FOLDER.toString().length() + 1);
            }
        }

        public void update(File f) {
            this.filePath = f;
            if (f == null) {
                button.setVisible(false);
                return;
            }
            selectedName = "-> " + f.getName();
            unselectedName = f.getName();
            button.setVisible(true);
            //			if(f.isDirectory()) {
            //				button.setTextColor(Utils.parseColor(folderColorProp.getPropValue()));
            //			}else {
            //				button.setTextColor(Utils.parseColor(fileColorProp.getPropValue()));
            //			}
            button.setText(f.getName());
        }

        public void setVisible(boolean b) {
            button.setVisible(b && filePath != null);
        }

        public GuiButton getButton() {
            return button;
        }

        public File getFilePath() {
            return filePath;
        }

    }

    private Gui requester;

    public void setSelectionMode(Gui requester, boolean selectionMode) {
        this.requester = requester;
        this.selectionMode = selectionMode;
    }

    private int getColumnCount() {
        LuaTable sets = Settings.settings;
        LuaValue v = sets.get("scriptBrowser");
        if (v == LuaValue.NIL) {
            sets.set("scriptBrowser", new LuaTable());
            v = sets.get("scriptBrowser");
        }
        LuaValue v2 = v.checktable().get("columns");
        if (v2 == LuaValue.NIL) {
            v.checktable().set("columns", 3);
        }
        return v.checktable().get("columns").checkint();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    private static enum PromptType {
        FileAction,
        FILE_NAME,
        FOLDER_NAME,
        RENAME;
    }

    private static enum FileActions {
        Rename, Cut, Copy, Run, DELETE;
        private static String[] nameArray;

        static {
            nameArray = new String[FileActions.values().length];
            FileActions[] ar = values();
            for (int i = 0; i < ar.length; i++) {
                nameArray[i] = ar[i].toString();
            }
        }

        public static String[] getActionList() {
            return nameArray;
        }
    }

    private ResultHandler rh;

    public void getSelection(Gui gui, ResultHandler rh) {
        setSelectionMode(requester, true);
        this.rh = rh;
        MinecraftClient.getInstance().setScreen(this);
    }

    public void setSelectedFile(String script) {
        if (script == null) {
            selectedFile = null;
            return;
        }
        selectedFile = new File(AdvancedMacros.MACROS_FOLDER, script);
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setActivePath(File activePath) {
        this.activePath = activePath;
        populateList(activePath);
    }

    public File getActivePath() {
        return activePath;
    }

}
