import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Escalonador{
	static BCP[] tabelaDeProcessos;
	static LinkedList<BCP> prontos;
	static LinkedList<BCP> bloqueados;
	public static void main(String[] args)
	{
		inicializa();		
	}

	static void inicializa()
	{
		tabelaDeProcessos = new BCP[10];

		for(int i = 1; i <= 10; i++)
		{
			String n = i==10?"10":"0"+i;
			tabelaDeProcessos[i] = new BCP("processos/"+n+".txt");
		}
	}
	
	 
}

enum Estado
{
	Pronto,
	Rodando,
	Bloqueado;
}

class BCP{

	BCP(String programa)
	{
		comandos = new LinkedList<String>();

		File arquivo = new File(programa);

		try
		{
			Scanner sc = new Scanner(arquivo);

			while(sc.hasNextLine())
			{
				System.out.println(sc.nextLine());
			}

			sc.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	int programCounter = 0;
	Estado estado;
	int x = 0;
	int y = 0;
	LinkedList<String> comandos;
}
