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

		while(prontos.size() > 0 || bloqueados.size() > 0)
		{
			//Atualiza bloqueados
			for(int i = 0; i < bloqueados.size(); i++)
			{
				int proximo = bloqueados.get(i);

				if(tabelaDeProcessos[proximo].atualizaES() == Estado.Pronto)
				{
					bloqueados.remove(i);
					prontos.add(proximo);
					System.out.println("Processo "+ (proximo+1) + " desbloqueado.");
				}
			}

			//Atualiza processo
			int proximo = prontos.remove();
			Estado saida = Estado.Pronto;
			System.out.println("Processo "+ (proximo+1) + " executando.");
			for(int i = 0; i < quantum; i++)
			{
				saida = tabelaDeProcessos[proximo].roda();

				if(saida == Estado.Fim || saida == Estado.Bloqueado)
					break;
			}

			switch(saida)
			{
				case Rodando:
					System.out.println("Processo "+ (proximo+1) + " executou e nao terminou.");
					tabelaDeProcessos[proximo].preempsao();
					prontos.add(proximo);
					break;
				case Bloqueado:
					System.out.println("Processo "+ (proximo+1) + " bloqueado.");
					bloqueados.add(proximo);
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

	boolean terminouES = false;
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

	public Estado atualizaES()
	{
		if(tempoDeEspera > 0)
		{
			tempoDeEspera--;
		}
		else
		{
			terminouES = true;
			estado = Estado.Pronto;
		}

		return estado;
	}

	public Estado roda()
	{
		estado = Estado.Rodando;
		String comando = comandos.get(programCounter);
		System.out.println(comando);

		if(comando.equals("SAIDA"))
		{
			estado = Estado.Fim;
		}
		if(comando.equals("E/S"))
		{
			if(terminouES)
			{
				terminouES = false;
				estado = Estado.Pronto;
			}
			else
			{
				estado = Estado.Bloqueado;
				tempoDeEspera = 2;
				return estado;
			}
		}

		programCounter++;
		return estado;
	}

	public void preempsao()
	{
		estado = Estado.Pronto;
	}
}
