package mg.itu.rohymvc.fichiermap;
public class MappingFile {
     String nomFichier;
     String extension;
     byte[] contenu;
     String cheminStockage;
   
     public String getNomFichier() {
         return nomFichier;
     }
     public void setNomFichier(String nomFichier) {
         this.nomFichier = nomFichier;
     }
     public String getExtension() {
         return extension;
     }
     public void setExtension(String extension) {
         this.extension = extension;
     }
     public byte[] getContenu() {
         return contenu;
     }
     public void setContenu(byte[] contenu) {
         this.contenu = contenu;
     }
     public void setCheminStockage(String string) {
        
     }
       public String getCheminStockage() {
        return cheminStockage;
    }
}
