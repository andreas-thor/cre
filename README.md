# CRE: Cited References Explorer

##ToDo
* performance improvements; handling of large data files
* documentation of source code
* website


##UPDATE 2015/11/19
1 settings dialog 
..* list of attributes is in three columns
..* year range values without separator (,)
..* digits for percent values
..* at least one chart line must be visible
1 last window position is stored (before exit) and loaded (on startup)
1 re-arranged Java/Groovy classes into packages "data" and "ui", respectively
1 "save as CSV" warning if file exists

##UPDATE 2015/11/16
* import by year range only (changed settings tab "miscellaneous" to "import") 
* label adjustes (to check!)
* remove by PERCENT YEAR threshold (new menu entry)

##UPDATE 2015/10/30
* revision of program settings (re-arranged in settings dialog, stored for next program start)
* maximum number of CRs for import can be set (0=infinite)
* "please wait" dialog has "cancel" button 

##UPDATES 2015/10/20
* Some classes are annotated as @CompileStatic for performance improvements
* Generated ant file and jnlp file for web starter deployment
* Removed matcher dialog
* Dialog windows centered relative to main window 

##UPDATES 2015/10/15
* Window position after start: center
* Name changed to CitedReferencesExplorer
* Dialog to save data before opening WoS / CSV file
* Hide clustering buttons after loading
* Remove vertical blue line at mouse position
* Clustering starts immediately after clicking "Cluster equivalent ..." (no dialog)
* Renaming: "source" for "medium" and "source title" for "title" 
* Make clustering insensitive to lower / upper case
    
## Clustering  
Explanation: Each ClusterId has two components x/y. 
If two CRs have the same clusterId (x/y) they are considered the same. 
If two CRs share the first component but have different second components (i.e., x/y' and x/y'') they are somewhat similar but no the same. 
In other words: The first component x reflects the result of a coarse-grained clustering.
When sorting by clusterId, CRs with different clusterIds sharing the same first component are close together which is helpful for manual inspection.
 
 
