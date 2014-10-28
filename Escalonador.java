import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Escalonador{
	static BCP[] tabelaDeProcessos;
	static LinkedList<Integer> prontos = new LinkedList<Integer>();
	static LinkedList<Integer> bloqueados = new LinkedList<Integer>();
	static int quantum = 0;
	public static void main(String[] args)
	{
		inicializa();

		while(prontos.size() > 0)
		{
			int proximo = prontos.remove();
			Estado saida = Estado.Pronto;
			System.out.println("Processo "+ (proximo+1) + " executando.");
			for(int i = 0; i < quantum; i++)
			{
				saida = tabelaDeProcessos[proximo].roda();

				if(saida == Estado.Fim)
					break;
			}

			switch(saida)
			{
				case Pronto:
					System.out.println("Processo "+ (proximo+1) + " executou e nao terminou.");
					prontos.add(proximo);
					break;

				case Fim:
					System.out.println("Processo "+(proximo+ 1) +" finalizado.");
					break;
			}
		}		
	}

	static void inicializa()
	{
		//Inicializa processos
		tabelaDeProcessos = new BCP[10];
		for(int i = 0; i < 10; i++)
		{
			String n = i==9?"10":"0"+(i+1);
			tabelaDeProcessos[i] = new BCP("processos/"+n+".txt");
			prontos.add(i);
		}

		//Carrega tempo de quantum
		File arquivoQuantum = new File("processos/quantum.txt");
		try
		{
			Scanner sc = new Scanner(arquivoQuantum);
			quantum = sc.nextInt();
			//System.out.println(quantum);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}

enum Estado
{
	Pronto,
	Rodando,
	Bloqueado,
	Fim;
}

class BCP{
	String nome;
	int programCounter = 0;
	Estado estado;
	int x = 0;
	int y = 0;
	LinkedList<String> comandos;

	int tempoDeEspera = 0;


	//Inicializa bloco de controle de processo (carrega de arquivo)
	BCP(String programa)
	{
		comandos = new LinkedList<String>();

		File arquivo = new File(programa);

		try
		{
			Scanner sc = new Scanner(arquivo);
			nome = sc.nextLine();

			while(sc.hasNextLine())
			{
				comandos.add(sc.nextLine());
				//System.out.println(sc.nextLine());
			}
			//System.out.println("C"+comandos.size());

			sc.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public Estado roda()
	{
		String comando = comandos.remove();
		System.out.println(comando);

		if(comando.equals("SAIDA"))
		{
			return Estado.Fim;
		}
		return Estado.Pronto;
	}
}
