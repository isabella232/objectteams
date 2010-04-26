team package org.eclipse.objectteams.example.fbapplication.BonusGUI;

/**
 * A dialog for presenting information about collected bonus credits.
 *
 * This class is a nested team, i.e., it is a role of its enclosing team BonusGUI
 * and at the same time it is the team for the contained roles Collector and Message.
 * As a role it uses the option of role files, i.e., roles stored in their own file.
 * You may use the package declaration to navigate (F3) to the enclosing team BonusGUI.
 */
protected team class FlightBonusDialog playedBy FlightBonus {

        /**
        * Message string to be placed in the dialog box
        */
        String message;	
        
        /**
        * Team/role constructor: Creates a new <code>FlightBonusDialog</code> object for the given 
        * <code>FlightBonus</code> object 
        */
        public FlightBonusDialog(FlightBonus fb) {
                this.initializeMessage(0);
                this.activate();
                System.out.println("FBDialog ");
        }
        
        /**
            * Store old number of credits for the next message.
            * @param credits
            */
        void initializeMessage(int credits) {
                this.message = new String("Collected credits in the past: "+credits+"\n");
        }
        
        /**
        *  When a subscriber is earning credits, the message string has to be updated.
        */
        protected abstract class Collector {
                
                /**
                    * Expected method: Returns the start string
                    */
                public abstract String getStart();
                
                /**
                    * Expected method: Returns the destination string
                    */
                public abstract String getDestination();
                
                /**
                    * Updates the message string when credits are calculated
                    */
                callin int recordCredits() {
                        int credits = base.recordCredits();
                        FlightBonusDialog.this.message += "FlightSegment: \n";
                        FlightBonusDialog.this.message += "    "+this.getStart()+"-->"+this.getDestination()+"\n";
                        FlightBonusDialog.this.message += "    earning credit: "+credits+"\n";
                        return credits;
                }
                
        }
        
        /**
        *  When a subscriber is buying something, the earned credits are shown in a dialog box.
        */
        protected abstract class Message {
                
                abstract int getTotalCollectedCredits();
                abstract String getName();
                
                /**
                    * Shows a dialog box with the bonus message
                    */
                public void showBonusDialog() {
                        int currentCredits = this.getTotalCollectedCredits();
                        
                        String title = "Bonus message for Passenger "+this.getName(); 
                        FlightBonusDialog.this.message += new String ("Collected credits now: "+currentCredits);
                        
                        JOptionPane.showMessageDialog(
                                                        BonusGUI.this.view.getComponent(), 
                                                        FlightBonusDialog.this.message, 
                                                        title, 
                                                        JOptionPane.INFORMATION_MESSAGE);

                        // reinitialize for the next message:
                        FlightBonusDialog.this.initializeMessage(currentCredits);
                }
        }
}
