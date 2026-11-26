package platformer.debug;

import platformer.core.Game;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.inventory.database.ItemDatabase;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.levels.LvlTriggerType;
import platformer.model.levels.metadata.WorldMapMetadata;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;

/**
 * Developer debug console and level teleporter opened via F5 when cheats are enabled.
 * Auto-discovers all rooms from world_map.json and interiors from InteriorManager, and provides one-click
 * time-of-day jumps, enemy spawning, flag toggling, item spawning, and combat testing utilities.
 */
public class DebugTeleporterDialog extends JDialog {

    private static DebugTeleporterDialog activeInstance;

    private enum TargetKind {
        ROOM, INTERIOR, ARENA
    }

    private record TeleportTarget(String id, TargetKind kind) {
        @Override
        public String toString() {
            return "[" + kind.name() + "]  " + id;
        }
    }

    private static final Color BG_DARK = new Color(16, 24, 28);
    private static final Color PANEL_DARK = new Color(24, 36, 42);
    private static final Color ACCENT_CYAN = new Color(56, 229, 223);
    private static final Color TEXT_LIGHT = new Color(228, 240, 240);

    private final GameContext context;
    private final List<TeleportTarget> allTargets = new ArrayList<>();
    private final DefaultListModel<TeleportTarget> listModel = new DefaultListModel<>();
    private final JList<TeleportTarget> targetList = new JList<>(listModel);
    private final JTextField tfSearch = new JTextField();
    private final JComboBox<LvlTriggerType> cbSpawn = new JComboBox<>(new LvlTriggerType[]{
            LvlTriggerType.SPAWN_A, LvlTriggerType.SPAWN_B, LvlTriggerType.SPAWN_C, LvlTriggerType.SPAWN_D
    });

    private final JComboBox<EnemyType> cbEnemyType = new JComboBox<>(
            Arrays.stream(EnemyType.values())
                    .filter(t -> t != EnemyType.MAX)
                    .toArray(EnemyType[]::new)
    );
    private final JSpinner spEnemyCount = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

    private final DefaultListModel<String> flagsModel = new DefaultListModel<>();
    private final JList<String> flagsList = new JList<>(flagsModel);
    private final JTextField tfFlag = new JTextField();
    private final JTextField tfItemId = new JTextField();
    private final JSpinner spItemAmount = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JLabel lbCurrentRoom = new JLabel();

    public static void toggle(GameContext context) {
        SwingUtilities.invokeLater(() -> {
            if (activeInstance != null && activeInstance.isDisplayable()) {
                activeInstance.dispose();
                activeInstance = null;
                return;
            }
            activeInstance = new DebugTeleporterDialog(context);
            activeInstance.setVisible(true);
        });
    }

    private DebugTeleporterDialog(GameContext context) {
        super((Frame) null, "Debug Teleporter & Dev Console (F5)", false);
        this.context = context;

        setAlwaysOnTop(true);
        setSize(800, 610);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));

        populateTargets();
        initUI();
        refreshStateLabels();
    }

    private void populateTargets() {
        allTargets.clear();

        WorldMapMetadata worldMap = context.getLevelManager().getWorldMap();
        if (worldMap != null && worldMap.getRooms() != null) {
            List<String> roomIds = new ArrayList<>(worldMap.getRooms().keySet());
            Collections.sort(roomIds);
            for (String roomId : roomIds) {
                allTargets.add(new TeleportTarget(roomId, TargetKind.ROOM));
            }
        }

        List<String> interiorIds = new ArrayList<>(context.getInteriorManager().getInteriorIds());
        Collections.sort(interiorIds);
        for (String interiorId : interiorIds) {
            allTargets.add(new TeleportTarget(interiorId, TargetKind.INTERIOR));
        }

        allTargets.add(new TeleportTarget("levelarena1", TargetKind.ARENA));
        applyFilter("");
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_DARK);
        header.setBorder(new EmptyBorder(8, 12, 8, 12));
        lbCurrentRoom.setForeground(ACCENT_CYAN);
        lbCurrentRoom.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.add(lbCurrentRoom, BorderLayout.WEST);

        JButton btnRefresh = createStyledButton("Refresh");
        btnRefresh.addActionListener(e -> refreshStateLabels());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTeleporterPanel(), createDevToolsPanel());
        split.setDividerLocation(370);
        split.setBorder(new EmptyBorder(0, 10, 10, 10));
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);
    }

    private JPanel createTeleporterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(PANEL_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setOpaque(false);
        JLabel lbFilter = new JLabel("Search:");
        lbFilter.setForeground(TEXT_LIGHT);
        tfSearch.setBackground(BG_DARK);
        tfSearch.setForeground(TEXT_LIGHT);
        tfSearch.setCaretColor(ACCENT_CYAN);
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter(tfSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter(tfSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter(tfSearch.getText());
            }
        });
        searchPanel.add(lbFilter, BorderLayout.WEST);
        searchPanel.add(tfSearch, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        targetList.setBackground(BG_DARK);
        targetList.setForeground(TEXT_LIGHT);
        targetList.setSelectionBackground(new Color(28, 82, 86));
        targetList.setSelectionForeground(Color.WHITE);
        targetList.setFont(new Font("Monospaced", Font.BOLD, 13));
        targetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        targetList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    teleportToSelected();
                }
            }
        });
        panel.add(new JScrollPane(targetList), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.setOpaque(false);

        JPanel spawnRow = new JPanel(new BorderLayout(6, 0));
        spawnRow.setOpaque(false);
        JLabel lbSpawn = new JLabel("Spawn Point:");
        lbSpawn.setForeground(TEXT_LIGHT);
        spawnRow.add(lbSpawn, BorderLayout.WEST);
        spawnRow.add(cbSpawn, BorderLayout.CENTER);

        JButton btnTeleport = createStyledButton("TELEPORT TO SELECTED");
        btnTeleport.setBackground(new Color(24, 125, 118));
        btnTeleport.addActionListener(e -> teleportToSelected());

        bottom.add(spawnRow, BorderLayout.NORTH);
        bottom.add(btnTeleport, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDevToolsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Time of Day
        panel.add(createSectionHeader("TIME OF DAY"));
        JPanel timeGrid = new JPanel(new GridLayout(1, 4, 4, 4));
        timeGrid.setOpaque(false);
        timeGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        timeGrid.add(createTimeButton("Dawn", 0.23));
        timeGrid.add(createTimeButton("Noon", 0.48));
        timeGrid.add(createTimeButton("Dusk", 0.74));
        timeGrid.add(createTimeButton("Night", 0.02));
        panel.add(timeGrid);
        panel.add(Box.createVerticalStrut(10));

        // Quick Actions
        panel.add(createSectionHeader("QUICK ACTIONS"));
        JPanel actionGrid = new JPanel(new GridLayout(2, 2, 6, 6));
        actionGrid.setOpaque(false);
        actionGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JButton btnHeal = createStyledButton("Full HP / Stamina / Ult");
        btnHeal.addActionListener(e -> runOnGameSync(() -> {
            Player p = context.getPlayer();
            p.changeHealth(p.getMaxHealth());
            p.changeStamina(p.getMaxStamina());
            p.getUltimateHandler().addCharge(100);
        }));

        JButton btnRich = createStyledButton("+10k Coins & +20 Tokens");
        btnRich.addActionListener(e -> runOnGameSync(() -> {
            context.getPlayer().changeCoins(10000);
            context.getPlayer().changeUpgradeTokens(20);
        }));

        JButton btnHitbox = createStyledButton("Toggle Hitboxes (F1)");
        btnHitbox.addActionListener(e -> DebugSettings.getInstance().setDebugMode(!DebugSettings.getInstance().isDebugMode()));

        JButton btnKillRoom = createStyledButton("Kill All Room Enemies");
        btnKillRoom.addActionListener(e -> runOnGameSync(() -> {
            for (Enemy enemy : context.getEnemyManager().getAllEnemies()) {
                if (enemy.isAlive() && !enemy.isBoss()) {
                    enemy.hit(99999, false, false);
                }
            }
        }));

        actionGrid.add(btnHeal);
        actionGrid.add(btnRich);
        actionGrid.add(btnHitbox);
        actionGrid.add(btnKillRoom);
        panel.add(actionGrid);
        panel.add(Box.createVerticalStrut(10));

        // Spawn Enemy
        panel.add(createSectionHeader("SPAWN ENEMY AHEAD OF PLAYER"));
        JPanel enemyRow = new JPanel(new BorderLayout(4, 0));
        enemyRow.setOpaque(false);
        enemyRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbEnemyType.setBackground(BG_DARK);
        cbEnemyType.setForeground(TEXT_LIGHT);
        JButton btnSpawnEnemy = createStyledButton("Spawn");
        btnSpawnEnemy.setBackground(new Color(24, 105, 115));
        btnSpawnEnemy.addActionListener(e -> {
            EnemyType type = (EnemyType) cbEnemyType.getSelectedItem();
            int count = (Integer) spEnemyCount.getValue();
            if (type == null) return;
            runOnGameSync(() -> {
                Player p = context.getPlayer();
                int dir = p.getFlipSign();
                int baseX = (int) (p.getHitBox().getCenterX() + dir * (2.5 * TILES_SIZE));
                int baseY = (int) p.getHitBox().y;
                for (int i = 0; i < count; i++) {
                    int offsetX = i * (int) (24 * SCALE) * dir;
                    context.getEnemyManager().spawnEnemy(type, baseX + offsetX, baseY);
                }
            });
            Logger.getInstance().notify("[DEBUG] Spawned " + count + "x " + type.name(), Message.INFORMATION);
        });
        enemyRow.add(cbEnemyType, BorderLayout.CENTER);
        JPanel enemyRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        enemyRight.setOpaque(false);
        spEnemyCount.setPreferredSize(new Dimension(52, 26));
        enemyRight.add(spEnemyCount);
        enemyRight.add(btnSpawnEnemy);
        enemyRow.add(enemyRight, BorderLayout.EAST);
        panel.add(enemyRow);
        panel.add(Box.createVerticalStrut(10));

        // Give Item
        panel.add(createSectionHeader("SPAWN ITEM (e.g. NIKOLAS_STOLEN_PACK)"));
        JPanel itemRow = new JPanel(new BorderLayout(4, 0));
        itemRow.setOpaque(false);
        itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tfItemId.setBackground(BG_DARK);
        tfItemId.setForeground(TEXT_LIGHT);
        tfItemId.setCaretColor(ACCENT_CYAN);
        JButton btnGiveItem = createStyledButton("Give");
        btnGiveItem.addActionListener(e -> {
            String itemId = tfItemId.getText().trim().toUpperCase();
            int amount = (Integer) spItemAmount.getValue();
            if (itemId.isEmpty() || ItemDatabase.getInstance().getItemData(itemId) == null) {
                JOptionPane.showMessageDialog(this, "Unknown Item ID: " + itemId, "Invalid Item", JOptionPane.WARNING_MESSAGE);
                return;
            }
            runOnGameSync(() -> context.getPlayer().getInventory().addItemToBackpack(new InventoryItem(itemId, amount)));
            Logger.getInstance().notify("[DEBUG] Spawned " + amount + "x " + itemId, Message.INFORMATION);
        });
        itemRow.add(tfItemId, BorderLayout.CENTER);
        JPanel itemRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        itemRight.setOpaque(false);
        spItemAmount.setPreferredSize(new Dimension(52, 26));
        itemRight.add(spItemAmount);
        itemRight.add(btnGiveItem);
        itemRow.add(itemRight, BorderLayout.EAST);
        panel.add(itemRow);
        panel.add(Box.createVerticalStrut(10));

        // World Flags
        panel.add(createSectionHeader("WORLD FLAGS"));
        JPanel flagRow = new JPanel(new BorderLayout(4, 0));
        flagRow.setOpaque(false);
        flagRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tfFlag.setBackground(BG_DARK);
        tfFlag.setForeground(TEXT_LIGHT);
        tfFlag.setCaretColor(ACCENT_CYAN);

        JPanel flagBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        flagBtns.setOpaque(false);
        JButton btnAddFlag = createStyledButton("+ Add");
        JButton btnRemoveFlag = createStyledButton("- Del");
        btnAddFlag.addActionListener(e -> {
            String flag = tfFlag.getText().trim();
            if (!flag.isEmpty()) {
                runOnGameSync(() -> {
                    context.getWorldMemoryManager().addFlag(flag);
                    context.getObjectManager().applyObjectMetadata();
                });
                refreshStateLabels();
            }
        });
        btnRemoveFlag.addActionListener(e -> {
            String flag = tfFlag.getText().trim();
            if (!flag.isEmpty()) {
                runOnGameSync(() -> {
                    context.getWorldMemoryManager().removeFlag(flag);
                    context.getObjectManager().applyObjectMetadata();
                });
                refreshStateLabels();
            }
        });
        flagBtns.add(btnAddFlag);
        flagBtns.add(btnRemoveFlag);
        flagRow.add(tfFlag, BorderLayout.CENTER);
        flagRow.add(flagBtns, BorderLayout.EAST);
        panel.add(flagRow);
        panel.add(Box.createVerticalStrut(6));

        flagsList.setBackground(BG_DARK);
        flagsList.setForeground(new Color(170, 235, 225));
        flagsList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        flagsList.addListSelectionListener(e -> {
            String sel = flagsList.getSelectedValue();
            if (sel != null) tfFlag.setText(sel);
        });
        JScrollPane flagsScroll = new JScrollPane(flagsList);
        panel.add(flagsScroll);

        return panel;
    }

    private JButton createTimeButton(String label, double normalizedTime) {
        JButton btn = createStyledButton(label);
        btn.addActionListener(e -> runOnGameSync(() -> {
            context.getTimeCycleManager().setLockedToNight(false);
            context.getTimeCycleManager().setGameTime(normalizedTime * TimeCycleManager.SECONDS_PER_GAME_DAY);
            context.getLightManager().snapToTimeCycle(context.getTimeCycleManager());
            context.getEffectManager().updateTimeOfDay(context.getTimeCycleManager());
        }));
        return btn;
    }

    private void teleportToSelected() {
        TeleportTarget selected = targetList.getSelectedValue();
        if (selected == null) return;
        LvlTriggerType spawn = (LvlTriggerType) cbSpawn.getSelectedItem();
        if (spawn == null) spawn = LvlTriggerType.SPAWN_A;

        final LvlTriggerType targetSpawn = spawn;
        runOnGameSync(() -> {
            switch (selected.kind()) {
                case ROOM -> {
                    context.getInteriorManager().clearInteriorState();
                    context.getWorldTransitionCoordinator().enterRoom(selected.id(), targetSpawn, null, null);
                }
                case INTERIOR -> {
                    context.getInteriorManager().loadInterior(selected.id() + ":" + targetSpawn.name());
                }
                case ARENA -> {
                    context.getInteriorManager().clearInteriorState();
                    context.getWorldTransitionCoordinator().enterArena();
                }
            }
        });

        Logger.getInstance().notify("[DEBUG] Teleported to " + selected, Message.INFORMATION);
        refreshStateLabels();
    }

    private void applyFilter(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        listModel.clear();
        for (TeleportTarget t : allTargets) {
            if (q.isEmpty() || t.toString().toLowerCase().contains(q)) {
                listModel.addElement(t);
            }
        }
        if (!listModel.isEmpty()) {
            targetList.setSelectedIndex(0);
        }
    }

    private void refreshStateLabels() {
        String currentLoc = context.getInteriorManager().isInterior()
                ? "INTERIOR: " + context.getInteriorManager().getCurrentInteriorId()
                : (context.getLevelManager().isInArena() ? "ARENA: levelarena1" : "ROOM: " + context.getLevelManager().getCurrentLevelId());
        lbCurrentRoom.setText("Current Location: " + currentLoc);

        flagsModel.clear();
        List<String> sortedFlags = new ArrayList<>(context.getWorldMemoryManager().getAllFlags());
        Collections.sort(sortedFlags);
        for (String f : sortedFlags) {
            flagsModel.addElement(f);
        }
    }

    private void runOnGameSync(Runnable action) {
        Game game = context.getGameState().getGame();
        synchronized (game) {
            action.run();
        }
    }

    private JLabel createSectionHeader(String title) {
        JLabel lb = new JLabel(title);
        lb.setForeground(ACCENT_CYAN);
        lb.setFont(new Font("SansSerif", Font.BOLD, 11));
        lb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lb;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(34, 52, 60));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(70, 120, 128)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}