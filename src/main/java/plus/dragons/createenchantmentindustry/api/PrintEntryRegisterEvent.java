package plus.dragons.createenchantmentindustry.api;

import net.minecraftforge.eventbus.api.Event;
import javax.annotation.Nonnull;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntries;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntry;

public class PrintEntryRegisterEvent extends Event {
    public void register(@Nonnull PrintEntry printEntry){
        if(PrintEntries.ENTRIES.put(printEntry.id(),printEntry)!=null)
            throw new IllegalArgumentException(printEntry.id() + "has already been registered!");
    }

}
