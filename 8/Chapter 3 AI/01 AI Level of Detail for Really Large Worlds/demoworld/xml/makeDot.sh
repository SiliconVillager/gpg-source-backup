for i in *.dt 
  do 
  if [ -z $last ]; then 
    last=$i;
    continue;
    fi
    
  if diff $i $last > /dev/null ; 
    then rm $i ; 
    else last=$i;
  fi
  
  done
  
for i in *.dt ; do dot -Tps $i > $i.ps ;  done

