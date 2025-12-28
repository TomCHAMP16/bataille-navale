package bataille_navale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

/**
 * MAIN CLASS
 */
public class BatailleNavale {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MenuGraphique().setVisible(true);
        });
    }
}

/**
 * THEME
 */
class Theme {
    // COULEURS PRINCIPALES (Palette Light / Océan Clair)
    public static final Color BACKGROUND_APP = new Color(240, 248, 255); // AliceBlue
    public static final Color BACKGROUND_CARD = Color.WHITE;

    // TEXTES
    public static final Color TEXT_TITLE = new Color(0, 80, 160);
    public static final Color TEXT_BODY = new Color(50, 60, 70);

    // UI ELEMENTS
    public static final Color PRIMARY_BTN = new Color(0, 110, 200);
    public static final Color SECONDARY_BTN = new Color(150, 160, 170);
    public static final Color SUCCESS_BTN = new Color(40, 180, 80);

    // GRILLE
    public static final Color GRID_WATER = new Color(250, 250, 255);
    public static final Color GRID_LINES = new Color(200, 220, 240);
    public static final Color GRID_SHIP = new Color(100, 110, 120);
    public static final Color GRID_HIT = new Color(220, 50, 50);
    public static final Color GRID_SUNK = new Color(50, 50, 50);
    public static final Color GRID_MISS = new Color(0, 150, 220);

    // FONTS
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
}

/**
 * NAVIRE
 */
class Navire {
    private String nom;
    private int taille;
    private int touches;

    public Navire(String nom, int taille) {
        this.nom = nom;
        this.taille = taille;
        this.touches = 0;
    }

    public void estTouche() {
        this.touches++;
    }

    public boolean estCoule() {
        return this.touches >= this.taille;
    }

    public String getNom() {
        return nom;
    }

    public int getTaille() {
        return taille;
    }
}

/**
 * GRILLE
 */
class Grille {
    public static final int TAILLE = 10;
    public static final int EAU = 0;
    public static final int NAVIRE = 1;
    public static final int LOUPE = 2;
    public static final int TOUCHE = 3;

    private int[][] matrice;
    private Navire[][] grilleNavires;

    public Grille() {
        this.matrice = new int[TAILLE][TAILLE];
        this.grilleNavires = new Navire[TAILLE][TAILLE];
    }

    public void vider() {
        for (int y = 0; y < TAILLE; y++) {
            for (int x = 0; x < TAILLE; x++) {
                this.matrice[y][x] = EAU;
                this.grilleNavires[y][x] = null;
            }
        }
    }

    public boolean peutPlacer(int taille, int x, int y, boolean horizontal) {
        if (horizontal) {
            if (x + taille > TAILLE)
                return false;
        } else {
            if (y + taille > TAILLE)
                return false;
        }

        for (int i = 0; i < taille; i++) {
            int cx = horizontal ? x + i : x;
            int cy = horizontal ? y : y + i;
            if (!estZoneLibre(cx, cy)) {
                return false;
            }
        }
        return true;
    }

    private boolean estZoneLibre(int cx, int cy) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = cx + dx;
                int ny = cy + dy;
                if (nx >= 0 && nx < TAILLE && ny >= 0 && ny < TAILLE) {
                    if (matrice[ny][nx] != EAU) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void placerNaviresAleatoirement(List<Navire> navires) {
        Random rand = new Random();
        for (Navire navire : navires) {
            boolean place = false;
            while (!place) {
                int x = rand.nextInt(TAILLE);
                int y = rand.nextInt(TAILLE);
                boolean horizontal = rand.nextBoolean();

                if (peutPlacer(navire.getTaille(), x, y, horizontal)) {
                    placerNavire(navire, x, y, horizontal);
                    place = true;
                }
            }
        }
    }

    public void placerNavire(Navire navire, int x, int y, boolean horizontal) {
        for (int i = 0; i < navire.getTaille(); i++) {
            int cx = horizontal ? x + i : x;
            int cy = horizontal ? y : y + i;
            matrice[cy][cx] = NAVIRE;
            grilleNavires[cy][cx] = navire;
        }
    }

    public boolean estDejaJoue(int x, int y) {
        if (!estDansGrille(x, y))
            return true;
        return matrice[y][x] == TOUCHE || matrice[y][x] == LOUPE;
    }

    public boolean recevoirTir(int x, int y) {
        if (!estDansGrille(x, y))
            return false;

        if (matrice[y][x] == NAVIRE) {
            matrice[y][x] = TOUCHE;
            if (grilleNavires[y][x] != null) {
                grilleNavires[y][x].estTouche();
            }
            return true;
        } else if (matrice[y][x] == EAU) {
            matrice[y][x] = LOUPE;
            return false;
        }
        return false;
    }

    public Navire getNavireEn(int x, int y) {
        if (!estDansGrille(x, y))
            return null;
        return grilleNavires[y][x];
    }

    private boolean estDansGrille(int x, int y) {
        return x >= 0 && x < TAILLE && y >= 0 && y < TAILLE;
    }

    public int getEtat(int x, int y) {
        return matrice[y][x];
    }

    public boolean estDefaite() {
        int casesRestantes = 0;
        for (int y = 0; y < TAILLE; y++) {
            for (int x = 0; x < TAILLE; x++) {
                if (matrice[y][x] == NAVIRE) {
                    casesRestantes++;
                }
            }
        }
        return casesRestantes == 0;
    }
}

/**
 * JEU (MOTEUR)
 */
class Jeu {
    public enum Phase {
        PLACEMENT, JEU, FIN
    }

    public enum NiveauDifficulte {
        FACILE, MOYENNE, DIFFICILE
    }

    private Grille grilleJ1;
    private Grille grilleJ2;
    private int joueurCourant;
    private Phase phaseCourante;
    private boolean modeIA = false;
    private NiveauDifficulte niveauIA = NiveauDifficulte.FACILE;
    private List<Navire> naviresAPlacerJ1;
    private List<Navire> naviresAPlacerJ2;
    private boolean orientationHorizontale = true;
    private javax.swing.JFrame fenetreJeu;
    private List<int[]> ciblesIA = new ArrayList<>();
    private String messageInfo = "Bienvenue ! Placez vos navires.";

    public Jeu() {
        this.grilleJ1 = new Grille();
        this.grilleJ2 = new Grille();
        this.joueurCourant = 1;
        this.phaseCourante = Phase.PLACEMENT;
        this.naviresAPlacerJ1 = initFlotte();
        this.naviresAPlacerJ2 = initFlotte();
    }

    public void setFenetreJeu(javax.swing.JFrame f) {
        this.fenetreJeu = f;
    }

    private List<Navire> initFlotte() {
        List<Navire> flotte = new ArrayList<>();
        flotte.add(new Navire("Cuirassé", 4));
        flotte.add(new Navire("Croiseur", 3));
        flotte.add(new Navire("Croiseur", 3));
        flotte.add(new Navire("Destroyer", 2));
        flotte.add(new Navire("Destroyer", 2));
        flotte.add(new Navire("Destroyer", 2));
        flotte.add(new Navire("Torpilleur", 1));
        flotte.add(new Navire("Torpilleur", 1));
        flotte.add(new Navire("Torpilleur", 1));
        flotte.add(new Navire("Torpilleur", 1));
        return flotte;
    }

    public Grille getGrilleJ1() {
        return grilleJ1;
    }

    public Grille getGrilleJ2() {
        return grilleJ2;
    }

    public int getJoueurCourant() {
        return joueurCourant;
    }

    public Phase getPhase() {
        return phaseCourante;
    }

    public boolean isOrientationHorizontale() {
        return orientationHorizontale;
    }

    public Navire getNavireCourant() {
        List<Navire> liste = (joueurCourant == 1) ? naviresAPlacerJ1 : naviresAPlacerJ2;
        if (liste.isEmpty())
            return null;
        return liste.get(0);
    }

    public List<Navire> getNaviresRestants(int joueurId) {
        return (joueurId == 1) ? naviresAPlacerJ1 : naviresAPlacerJ2;
    }

    public void basculerOrientation() {
        this.orientationHorizontale = !this.orientationHorizontale;
    }

    public boolean placerNavireJoueur(int x, int y) {
        if (phaseCourante != Phase.PLACEMENT)
            return false;
        Grille grilleActuelle = (joueurCourant == 1) ? grilleJ1 : grilleJ2;
        List<Navire> naviresRestants = (joueurCourant == 1) ? naviresAPlacerJ1 : naviresAPlacerJ2;
        if (naviresRestants.isEmpty())
            return false;

        Navire navireModel = naviresRestants.get(0);
        if (grilleActuelle.peutPlacer(navireModel.getTaille(), x, y, orientationHorizontale)) {
            grilleActuelle.placerNavire(navireModel, x, y, orientationHorizontale);
            naviresRestants.remove(0);
            verifierFinPlacement();
            return true;
        }
        return false;
    }

    public String getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(String msg) {
        this.messageInfo = msg;
    }

    private void verifierFinPlacement() {
        if (joueurCourant == 1 && naviresAPlacerJ1.isEmpty()) {
            if (modeIA) {
                grilleJ2.placerNaviresAleatoirement(naviresAPlacerJ2);
                naviresAPlacerJ2.clear();
                this.messageInfo = "L'IA a placé ses navires ! LA BATAILLE COMMENCE !";
                phaseCourante = Phase.JEU;
                joueurCourant = 1;
            } else {
                this.messageInfo = "J1 TERMINÉ ! Au tour de J2 (Cachez l'écran !)";
                joueurCourant = 2;
            }
        } else if (joueurCourant == 2 && naviresAPlacerJ2.isEmpty()) {
            this.messageInfo = "TOUS LES NAVIRES SONT PLACÉS ! LA BATAILLE COMMENCE !";
            phaseCourante = Phase.JEU;
            joueurCourant = 1;
        }
    }

    public void jouerTour(int x, int y) {
        if (phaseCourante != Phase.JEU)
            return;
        Grille grilleCible = (joueurCourant == 1) ? grilleJ2 : grilleJ1;
        boolean resultat = grilleCible.recevoirTir(x, y);

        if (resultat) {
            this.messageInfo = "TOUCHÉ ! Le Joueur " + joueurCourant + " rejoue.";
            if (modeIA && joueurCourant == 2
                    && (niveauIA == NiveauDifficulte.DIFFICILE || niveauIA == NiveauDifficulte.MOYENNE)) {
                ajouterCiblesVoisines(x, y, grilleCible);
            }
            Navire navireTouche = grilleCible.getNavireEn(x, y);
            if (navireTouche != null && navireTouche.estCoule()) {
                String nomAttaquant = (joueurCourant == 1) ? "Joueur 1" : (modeIA ? "L'IA" : "Joueur 2");
                this.messageInfo = "BOUM ! " + nomAttaquant + " a COULÉ le " + navireTouche.getNom() + " !";
                if (modeIA && joueurCourant == 2 && niveauIA == NiveauDifficulte.DIFFICILE) {
                    marquerAutourCoule(navireTouche, grilleCible);
                }
            }
            if (grilleCible.estDefaite()) {
                gererFinDePartie(joueurCourant);
            }
        } else {
            this.messageInfo = "PLOUF (À l'eau). C'est au tour de l'adversaire.";
            joueurCourant = (joueurCourant == 1) ? 2 : 1;
        }
    }

    private void ajouterCiblesVoisines(int cx, int cy, Grille grilleCible) {
        int[][] directions = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
        for (int[] d : directions) {
            int nx = cx + d[0];
            int ny = cy + d[1];
            if (nx >= 0 && nx < 10 && ny >= 0 && ny < 10) {
                if (!grilleCible.estDejaJoue(nx, ny)) {
                    if (niveauIA == NiveauDifficulte.DIFFICILE && estCoupInutile(nx, ny, grilleCible))
                        continue;
                    boolean dejaPrevu = false;
                    for (int[] cible : ciblesIA) {
                        if (cible[0] == nx && cible[1] == ny) {
                            dejaPrevu = true;
                            break;
                        }
                    }
                    if (!dejaPrevu)
                        ciblesIA.add(new int[] { nx, ny });
                }
            }
        }
    }

    private void gererFinDePartie(int vainqueur) {
        phaseCourante = Phase.FIN;
        Object[] options = { "Rejouer", "Quitter" };
        int choix = JOptionPane.showOptionDialog(null,
                "FÉLICITATIONS !\n\nLe JOUEUR " + vainqueur + " a gagné la partie !",
                "Victoire", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choix == JOptionPane.YES_OPTION) {
            reinitialiserPartie();
        } else {
            if (this.fenetreJeu != null)
                this.fenetreJeu.dispose();
            new MenuGraphique().setVisible(true);
        }
    }

    private void reinitialiserPartie() {
        grilleJ1.vider();
        grilleJ2.vider();
        joueurCourant = 1;
        phaseCourante = Phase.PLACEMENT;
        orientationHorizontale = true;
        ciblesIA.clear();
        naviresAPlacerJ1 = initFlotte();
        naviresAPlacerJ2 = initFlotte();
    }

    public void setModeIA(NiveauDifficulte niveau) {
        this.modeIA = true;
        this.niveauIA = niveau;
    }

    public boolean isModeIA() {
        return modeIA;
    }

    private boolean estCoupInutile(int x, int y, Grille grille) {
        int[][] diagonales = { { -1, -1 }, { 1, -1 }, { -1, 1 }, { 1, 1 } };
        for (int[] d : diagonales) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 0 && nx < 10 && ny >= 0 && ny < 10) {
                if (grille.getEtat(nx, ny) == Grille.TOUCHE)
                    return true;
            }
        }
        return false;
    }

    private void marquerAutourCoule(Navire navire, Grille grille) {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (grille.getNavireEn(x, y) == navire) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < 10 && ny >= 0 && ny < 10) {
                                if (grille.getEtat(nx, ny) == Grille.EAU) {
                                    grille.recevoirTir(nx, ny);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void executerTourIA() {
        if (phaseCourante != Phase.JEU || joueurCourant != 2)
            return;
        Grille grilleAdverse = grilleJ1;
        int tirX = -1, tirY = -1;

        if ((niveauIA == NiveauDifficulte.DIFFICILE || niveauIA == NiveauDifficulte.MOYENNE) && !ciblesIA.isEmpty()) {
            int index = ciblesIA.size() - 1;
            int[] cible = ciblesIA.remove(index);
            tirX = cible[0];
            tirY = cible[1];
            if (grilleAdverse.estDejaJoue(tirX, tirY)) {
                executerTourIA();
                return;
            }
        } else {
            boolean valide = false;
            int essais = 0;
            while (!valide && essais < 200) {
                tirX = (int) (Math.random() * 10);
                tirY = (int) (Math.random() * 10);
                essais++;
                if (!grilleAdverse.estDejaJoue(tirX, tirY)) {
                    if (niveauIA == NiveauDifficulte.DIFFICILE && estCoupInutile(tirX, tirY, grilleAdverse))
                        continue;
                    valide = true;
                }
            }
            if (!valide) {
                for (int y = 0; y < 10; y++) {
                    for (int x = 0; x < 10; x++) {
                        if (!grilleAdverse.estDejaJoue(x, y)) {
                            tirX = x;
                            tirY = y;
                            valide = true;
                            break;
                        }
                    }
                    if (valide)
                        break;
                }
            }
        }
        jouerTour(tirX, tirY);
    }
}

/**
 * GRILLE PANEL (UI)
 */
class GrillePanel extends JPanel {
    private Grille grille;
    private Jeu jeu;
    private int idJoueurProprietaire;
    private final int TAILLE_CASE = 30;
    private final int MARGE_X = 30;
    private final int MARGE_Y = 30;
    private int sourisX = -1;
    private int sourisY = -1;

    public GrillePanel(Grille grille, Jeu jeu, int idJoueurProprietaire) {
        this.grille = grille;
        this.jeu = jeu;
        this.idJoueurProprietaire = idJoueurProprietaire;
        this.setPreferredSize(
                new Dimension(Grille.TAILLE * TAILLE_CASE + MARGE_X + 1, Grille.TAILLE * TAILLE_CASE + MARGE_Y + 1));
        this.setBackground(new Color(0, 0, 0, 0));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gererClic(e.getX(), e.getY(), e.getButton());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sourisX = -1;
                sourisY = -1;
                repaint();
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                sourisX = (e.getX() - MARGE_X) / TAILLE_CASE;
                sourisY = (e.getY() - MARGE_Y) / TAILLE_CASE;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Theme.BACKGROUND_APP);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(new Color(150, 160, 170));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        for (int x = 0; x < Grille.TAILLE; x++) {
            String lettre = String.valueOf((char) ('A' + x));
            int textX = MARGE_X + x * TAILLE_CASE + (TAILLE_CASE - g2d.getFontMetrics().stringWidth(lettre)) / 2;
            g2d.drawString(lettre, textX, MARGE_Y - 8);
        }
        for (int y = 0; y < Grille.TAILLE; y++) {
            String chiffre = String.valueOf(y + 1);
            int textY = MARGE_Y + y * TAILLE_CASE + (TAILLE_CASE + g2d.getFontMetrics().getAscent()) / 2 - 4;
            g2d.drawString(chiffre, 5, textY);
        }

        g2d.translate(MARGE_X, MARGE_Y);
        for (int y = 0; y < Grille.TAILLE; y++) {
            for (int x = 0; x < Grille.TAILLE; x++) {
                int etat = grille.getEtat(x, y);
                int px = x * TAILLE_CASE;
                int py = y * TAILLE_CASE;

                g2d.setColor(Theme.GRID_WATER);
                g2d.fillRect(px, py, TAILLE_CASE, TAILLE_CASE);
                g2d.setColor(Theme.GRID_LINES);
                g2d.drawRect(px, py, TAILLE_CASE, TAILLE_CASE);

                switch (etat) {
                    case Grille.NAVIRE:
                        boolean visible = false;
                        if (jeu.getPhase() == Jeu.Phase.PLACEMENT && jeu.getJoueurCourant() == idJoueurProprietaire)
                            visible = true;
                        if (jeu.getPhase() == Jeu.Phase.FIN)
                            visible = true;
                        if (visible) {
                            g2d.setColor(Theme.GRID_SHIP);
                            g2d.fillRoundRect(px + 4, py + 4, TAILLE_CASE - 8, TAILLE_CASE - 8, 4, 4);
                        }
                        break;
                    case Grille.LOUPE:
                        g2d.setColor(Theme.GRID_MISS);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawOval(px + 8, py + 8, TAILLE_CASE - 16, TAILLE_CASE - 16);
                        g2d.setStroke(new BasicStroke(1));
                        break;
                    case Grille.TOUCHE:
                        Navire n = grille.getNavireEn(x, y);
                        boolean estCoule = (n != null && n.estCoule());
                        if (estCoule) {
                            g2d.setColor(Theme.GRID_SUNK);
                            g2d.fillRect(px, py, TAILLE_CASE, TAILLE_CASE);
                            g2d.setColor(new Color(200, 50, 50));
                            g2d.setStroke(new BasicStroke(2));
                            g2d.drawLine(px + 4, py + 4, px + TAILLE_CASE - 4, py + TAILLE_CASE - 4);
                            g2d.drawLine(px + TAILLE_CASE - 4, py + 4, px + 4, py + TAILLE_CASE - 4);
                        } else {
                            g2d.setColor(new Color(255, 230, 230));
                            g2d.fillRect(px, py, TAILLE_CASE, TAILLE_CASE);
                            g2d.setColor(Theme.GRID_HIT);
                            g2d.setStroke(new BasicStroke(3));
                            g2d.drawLine(px + 6, py + 6, px + TAILLE_CASE - 6, py + TAILLE_CASE - 6);
                            g2d.drawLine(px + TAILLE_CASE - 6, py + 6, px + 6, py + TAILLE_CASE - 6);
                        }
                        g2d.setStroke(new BasicStroke(1));
                        break;
                }
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.drawRect(px, py, TAILLE_CASE, TAILLE_CASE);
            }
        }
        dessinerGhost(g2d);

        if (jeu != null) {
            if (jeu.getPhase() == Jeu.Phase.PLACEMENT && jeu.getJoueurCourant() == idJoueurProprietaire) {
                g2d.setColor(new Color(0, 255, 0, 200));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(1, 1, Grille.TAILLE * TAILLE_CASE - 2, Grille.TAILLE * TAILLE_CASE - 2);
            } else if (jeu.getPhase() == Jeu.Phase.JEU && jeu.getJoueurCourant() != idJoueurProprietaire) {
                g2d.setColor(new Color(255, 50, 50, 200));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(1, 1, Grille.TAILLE * TAILLE_CASE - 2, Grille.TAILLE * TAILLE_CASE - 2);
            }
        }
    }

    private void dessinerGhost(Graphics2D g2d) {
        if (jeu.getPhase() == Jeu.Phase.PLACEMENT &&
                jeu.getJoueurCourant() == idJoueurProprietaire &&
                sourisX >= 0 && sourisX < Grille.TAILLE && sourisY >= 0 && sourisY < Grille.TAILLE) {
            Navire navireCourant = jeu.getNavireCourant();
            if (navireCourant != null) {
                int taille = navireCourant.getTaille();
                boolean horizontal = jeu.isOrientationHorizontale();
                boolean valide = grille.peutPlacer(taille, sourisX, sourisY, horizontal);
                g2d.setColor(valide ? new Color(100, 255, 100, 150) : new Color(255, 100, 100, 150));
                for (int i = 0; i < taille; i++) {
                    int cx = horizontal ? sourisX + i : sourisX;
                    int cy = horizontal ? sourisY : sourisY + i;
                    if (cx < Grille.TAILLE && cy < Grille.TAILLE) {
                        g2d.fillRoundRect(cx * TAILLE_CASE + 2, cy * TAILLE_CASE + 2, TAILLE_CASE - 4, TAILLE_CASE - 4,
                                10, 10);
                    }
                }
            }
        }
    }

    private void gererClic(int pixelX, int pixelY, int boutonSouris) {
        if (jeu == null)
            return;
        int x = (pixelX - MARGE_X) / TAILLE_CASE;
        int y = (pixelY - MARGE_Y) / TAILLE_CASE;

        if (x >= 0 && x < Grille.TAILLE && y >= 0 && y < Grille.TAILLE) {
            if (jeu.getPhase() == Jeu.Phase.PLACEMENT) {
                if (jeu.getJoueurCourant() == idJoueurProprietaire) {
                    if (boutonSouris == MouseEvent.BUTTON3) {
                        jeu.basculerOrientation();
                        repaint();
                    } else {
                        jeu.placerNavireJoueur(x, y);
                    }
                    redessinerInterface();
                }
            } else if (jeu.getPhase() == Jeu.Phase.JEU) {
                if (jeu.getJoueurCourant() != idJoueurProprietaire) {
                    if (boutonSouris == MouseEvent.BUTTON1) {
                        if (grille.estDejaJoue(x, y))
                            return;
                        jeu.jouerTour(x, y);
                        redessinerInterface();
                    }
                }
            }
        }
    }

    private void redessinerInterface() {
        this.repaint();
        Container parent = this.getParent();
        while (parent != null) {
            parent.repaint();
            parent = parent.getParent();
        }
    }
}

/**
 * MENU GRAPHIQUE
 */
class MenuGraphique extends JFrame {
    static class CardPanel extends JPanel {
        public CardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Theme.BACKGROUND_CARD);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        }
    }

    static class HeaderLabel extends JLabel {
        public HeaderLabel(String text) {
            super(text, SwingConstants.CENTER);
            setFont(Theme.FONT_TITLE);
            setForeground(Theme.TEXT_TITLE);
        }
    }

    static class StyledButton extends JButton {
        private Color baseColor;
        private Color hoverColor;

        public StyledButton(String text, Color bg) {
            super(text);
            this.baseColor = bg;
            this.hoverColor = bg.darker();
            setFont(Theme.FONT_BOLD);
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    setBackground(hoverColor);
                    repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    setBackground(baseColor);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover())
                g2d.setColor(hoverColor);
            else
                g2d.setColor(baseColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
        }
    }

    class SidebarNavires extends JPanel {
        private Jeu jeu;

        public SidebarNavires(Jeu jeu) {
            this.jeu = jeu;
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setOpaque(false);
            this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        public void updateList() {
            this.removeAll();
            JLabel lblTitre = new JLabel("Navires à placer");
            lblTitre.setFont(Theme.FONT_SUBTITLE);
            lblTitre.setForeground(Theme.TEXT_TITLE);
            lblTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(lblTitre);
            add(Box.createVerticalStrut(20));

            List<Navire> restants = jeu.getNaviresRestants(jeu.getJoueurCourant());

            if (jeu.getPhase() == Jeu.Phase.PLACEMENT) {
                JLabel lblHint = new JLabel("<html><i>Clic Droit pour tourner</i></html>");
                lblHint.setFont(Theme.FONT_NORMAL);
                lblHint.setForeground(Theme.PRIMARY_BTN);
                add(lblHint);
                add(Box.createVerticalStrut(10));
                if (restants.isEmpty()) {
                    add(creerItemNavire("Tous placés !", 0, false));
                } else {
                    boolean first = true;
                    for (Navire n : restants) {
                        add(creerItemNavire(n.getNom(), n.getTaille(), first));
                        add(Box.createVerticalStrut(10));
                        first = false;
                    }
                }
            } else {
                JLabel lblInfo = new JLabel("Combat en cours...");
                lblInfo.setFont(Theme.FONT_NORMAL);
                add(lblInfo);
            }
            this.revalidate();
            this.repaint();
        }

        private JPanel creerItemNavire(String nom, int taille, boolean actif) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(true);
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(actif ? Theme.PRIMARY_BTN : new Color(230, 230, 230), actif ? 2 : 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            p.setMaximumSize(new Dimension(300, 60));
            p.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lNom = new JLabel(nom);
            lNom.setFont(Theme.FONT_BOLD);
            lNom.setForeground(actif ? Theme.TEXT_TITLE : Theme.TEXT_BODY);
            JLabel lTaille = new JLabel(taille > 0 ? taille + " cases" : "");
            lTaille.setFont(Theme.FONT_NORMAL);
            lTaille.setForeground(Color.GRAY);

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            textPanel.add(lNom);
            textPanel.add(lTaille);

            JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            iconPanel.setOpaque(false);
            for (int i = 0; i < taille; i++) {
                JLabel block = new JLabel();
                block.setOpaque(true);
                block.setBackground(actif ? Theme.PRIMARY_BTN : Color.LIGHT_GRAY);
                block.setPreferredSize(new Dimension(12, 12));
                iconPanel.add(block);
            }
            p.add(iconPanel, BorderLayout.WEST);
            p.add(textPanel, BorderLayout.CENTER);
            return p;
        }
    }

    class SidebarInfo extends JPanel {
        private Jeu jeu;
        private JTextArea textArea;

        public SidebarInfo(Jeu jeu) {
            this.jeu = jeu;
            this.setLayout(new BorderLayout());
            this.setOpaque(false);
            this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            JLabel lblTitre = new JLabel("Journal de bord");
            lblTitre.setFont(Theme.FONT_SUBTITLE);
            lblTitre.setForeground(Theme.TEXT_TITLE);
            add(lblTitre, BorderLayout.NORTH);

            textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(Theme.FONT_NORMAL);
            textArea.setForeground(Theme.TEXT_BODY);
            textArea.setBackground(Theme.BACKGROUND_CARD);
            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            add(scroll, BorderLayout.CENTER);
        }

        public void log(String msg) {
            textArea.append(msg + "\n\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    private boolean enAttenteIA = false;
    private String lastLoggedMessage = "";

    public MenuGraphique() {
        setTitle("Bataille Navale");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BACKGROUND_APP);
        afficherMenuPrincipal();
    }

    private void afficherMenuPrincipal() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());
        CardPanel card = new CardPanel();
        card.setLayout(new GridLayout(0, 1, 10, 20));
        card.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        card.setPreferredSize(new Dimension(450, 400));

        JLabel logo = new JLabel("\u2693", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        logo.setForeground(Theme.TEXT_BODY);
        card.add(logo);
        card.add(new HeaderLabel("BATAILLE NAVALE"));
        JLabel sousTitre = new JLabel("Affrontez l'ordinateur dans un combat naval stratégique.",
                SwingConstants.CENTER);
        sousTitre.setFont(Theme.FONT_NORMAL);
        sousTitre.setForeground(Color.GRAY);
        card.add(sousTitre);

        StyledButton btnPvIA = new StyledButton("▶ Jouer contre l'IA", Theme.PRIMARY_BTN);
        StyledButton btnMulti = new StyledButton("Multijoueur (Local)", new Color(0, 150, 80));
        StyledButton btnRegles = new StyledButton("ℹ Règles du jeu", new Color(240, 240, 245));
        btnRegles.setForeground(Theme.TEXT_BODY);

        btnPvIA.addActionListener(e -> afficherMenuDifficulte());
        btnMulti.addActionListener(e -> lancerJeu(null));
        btnRegles.addActionListener(e -> afficherRegles());

        card.add(btnPvIA);
        card.add(btnMulti);
        card.add(btnRegles);
        add(card);
        revalidate();
        repaint();
    }

    private void afficherMenuDifficulte() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());
        CardPanel card = new CardPanel();
        card.setLayout(new GridLayout(0, 1, 10, 20));
        card.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        card.setPreferredSize(new Dimension(450, 400));
        card.add(new HeaderLabel("DIFFICULTÉ"));

        StyledButton btnFacile = new StyledButton("Facile", Theme.SUCCESS_BTN);
        StyledButton btnMoyen = new StyledButton("Moyenne", new Color(240, 180, 50));
        StyledButton btnDifficile = new StyledButton("Difficile", new Color(220, 50, 50));
        StyledButton btnRetour = new StyledButton("Retour", Theme.SECONDARY_BTN);

        btnFacile.addActionListener(e -> lancerJeu(Jeu.NiveauDifficulte.FACILE));
        btnMoyen.addActionListener(e -> lancerJeu(Jeu.NiveauDifficulte.MOYENNE));
        btnDifficile.addActionListener(e -> lancerJeu(Jeu.NiveauDifficulte.DIFFICILE));
        btnRetour.addActionListener(e -> afficherMenuPrincipal());

        card.add(btnFacile);
        card.add(btnMoyen);
        card.add(btnDifficile);
        card.add(btnRetour);
        add(card);
        revalidate();
        repaint();
    }

    private void afficherRegles() {
        JOptionPane.showMessageDialog(this,
                "Règles : \n1. Placez vos navires.\n2. Tirez sur l'adversaire.\n3. Coulez tout pour gagner !");
    }

    private void lancerJeu(Jeu.NiveauDifficulte niveau) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        Jeu jeu = new Jeu();
        if (niveau != null)
            jeu.setModeIA(niveau);
        jeu.setFenetreJeu(this);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Theme.BACKGROUND_APP);
        StyledButton btnRetour = new StyledButton("← Retour au menu", new Color(220, 230, 240));
        btnRetour.setForeground(Theme.TEXT_BODY);
        btnRetour.setPreferredSize(new Dimension(150, 40));
        btnRetour.addActionListener(e -> afficherMenuPrincipal());
        JLabel titreJeu = new JLabel("BATAILLE NAVALE", SwingConstants.CENTER);
        titreJeu.setFont(Theme.FONT_TITLE);
        titreJeu.setForeground(Theme.TEXT_TITLE);
        titreJeu.setPreferredSize(new Dimension(600, 50));
        header.add(btnRetour);
        header.add(titreJeu);
        add(header, BorderLayout.NORTH);

        JPanel contentArea = new JPanel(new GridBagLayout());
        contentArea.setBackground(Theme.BACKGROUND_APP);
        GrillePanel panelJ1 = new GrillePanel(jeu.getGrilleJ1(), jeu, 1);
        panelJ1.setBorder(BorderFactory.createLineBorder(Theme.GRID_LINES, 1));
        GrillePanel panelJ2 = new GrillePanel(jeu.getGrilleJ2(), jeu, 2);
        panelJ2.setBorder(BorderFactory.createLineBorder(Theme.GRID_LINES, 1));
        JPanel grillesContainer = new JPanel();
        grillesContainer.setOpaque(false);
        grillesContainer.add(panelJ1);
        contentArea.add(grillesContainer);
        add(contentArea, BorderLayout.CENTER);

        CardPanel sidebarInfoCard = new CardPanel();
        sidebarInfoCard.setLayout(new BorderLayout());
        sidebarInfoCard.setPreferredSize(new Dimension(250, getHeight()));
        sidebarInfoCard.setBackground(Color.WHITE);
        SidebarInfo sidebarInfo = new SidebarInfo(jeu);
        sidebarInfoCard.add(sidebarInfo, BorderLayout.CENTER);
        JPanel sidebarInfoContainer = new JPanel(new BorderLayout());
        sidebarInfoContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 0));
        sidebarInfoContainer.setOpaque(false);
        sidebarInfoContainer.add(sidebarInfoCard, BorderLayout.CENTER);
        add(sidebarInfoContainer, BorderLayout.WEST);

        CardPanel sidebarNavCard = new CardPanel();
        sidebarNavCard.setLayout(new BorderLayout());
        sidebarNavCard.setPreferredSize(new Dimension(250, getHeight()));
        sidebarNavCard.setBackground(Color.WHITE);
        SidebarNavires listNavires = new SidebarNavires(jeu);
        sidebarNavCard.add(listNavires, BorderLayout.CENTER);
        JPanel sidebarNavContainer = new JPanel(new BorderLayout());
        sidebarNavContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));
        sidebarNavContainer.setOpaque(false);
        sidebarNavContainer.add(sidebarNavCard, BorderLayout.CENTER);
        add(sidebarNavContainer, BorderLayout.EAST);

        new Timer(100, e -> {
            listNavires.updateList();
            String currentMsg = jeu.getMessageInfo();
            if (currentMsg != null && !currentMsg.isEmpty() && !currentMsg.equals(lastLoggedMessage)) {
                sidebarInfo.log(currentMsg);
                lastLoggedMessage = currentMsg;
            }

            if (jeu.getPhase() == Jeu.Phase.PLACEMENT) {
                if (jeu.getJoueurCourant() == 1) {
                    if (!grillesContainer.isAncestorOf(panelJ1) || grillesContainer.isAncestorOf(panelJ2)) {
                        grillesContainer.removeAll();
                        grillesContainer.add(panelJ1);
                        grillesContainer.revalidate();
                        grillesContainer.repaint();
                    }
                } else if (jeu.getJoueurCourant() == 2) {
                    if (!grillesContainer.isAncestorOf(panelJ2) || grillesContainer.isAncestorOf(panelJ1)) {
                        grillesContainer.removeAll();
                        grillesContainer.add(panelJ2);
                        grillesContainer.revalidate();
                        grillesContainer.repaint();
                    }
                }
            } else if (jeu.getPhase() == Jeu.Phase.JEU) {
                if (grillesContainer.getComponentCount() < 2) {
                    grillesContainer.removeAll();
                    grillesContainer.add(panelJ1);
                    grillesContainer.add(panelJ2);
                    grillesContainer.revalidate();
                    grillesContainer.repaint();
                }
            }

            if (jeu.isModeIA() && jeu.getJoueurCourant() == 2 && jeu.getPhase() == Jeu.Phase.JEU) {
                if (!enAttenteIA) {
                    enAttenteIA = true;
                    Timer delaiIA = new Timer(200, evt -> {
                        jeu.executerTourIA();
                        enAttenteIA = false;
                        ((Timer) evt.getSource()).stop();
                    });
                    delaiIA.setRepeats(false);
                    delaiIA.start();
                }
            }
        }).start();

        revalidate();
        repaint();
    }
}
