import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Escalonador{
	static BCP[] tabelaDeProcessos;
	static LinkedList<Integer> prontos = new LinkedList<Integer>();
	static LinkedList<Integer> bloqueados = new LinkedList<Integer>();
	static int quantum = 0;

	public static void main(String[] args)
	{
		try{

			//Carrega tempo de quantum
			File arquivoQuantum = new File("processos/quantum.txt");
			Scanner sc = new Scanner(arquivoQuantum);
			quantum = sc.nextInt();
			sc.close();
			//System.out.println(quantum);
			
			
			

			//Inicializa Writers para log
			String nomeDoArquivo = "log"+(quantum < 10?"0"+quantum:quantum)+ ".txt";
			FileWriter writer = new FileWriter(nomeDoArquivo);
			PrintWriter saida = new PrintWriter(writer);


			//Inicializa processos
			tabelaDeProcessos = new BCP[10];
			for(int i = 0; i < 10; i++)
			{
				String n = i==9?"10":"0"+(i+1);
				tabelaDeProcessos[i] = new BCP("processos/"+n+".txt");
				prontos.add(i);
				saida.println("Carregando " + tabelaDeProcessos[i].nome);
			}

			//ESCALONADOR
			while(prontos.size() > 0 || bloqueados.size() > 0)
			{
				//Atualiza processo
				int proximo = -1;
				proximo = prontos.remove();
				Estado estadoSaida = Estado.Pronto;
				saida.println("Executando "+tabelaDeProcessos[proximo].nome);
				for(int i = 0; i < quantum; i++)
				{
					estadoSaida = tabelaDeProcessos[proximo].roda();

					if(estadoSaida == Estado.Fim)
					{
						break;
					}

					if(estadoSaida == Estado.Bloqueado)
					{
						saida.println("Interrompendo "+ tabelaDeProcessos[proximo].nome + " após " + i + (i>0?" intruções.":" instrução."));
						break;
					}
				}

				switch(estadoSaida)
				{
					case Rodando:
						
						tabelaDeProcessos[proximo].preempsao();
						prontos.add(proximo);
						break;
					case Bloqueado:
					
						bloqueados.add(proximo);
						break;

					case Fim:
					
						saida.println(tabelaDeProcessos[proximo].nome + " terminado. X="+tabelaDeProcessos[proximo].x + ". Y="+tabelaDeProcessos[proximo].y);
						break;
				}


				//Atualiza bloqueados
				for(int i = 0; i < bloqueados.size(); i++)
				{
					int bloqueado = bloqueados.get(i);
					Estado estadoES = Estado.Bloqueado;

					if(bloqueado != proximo)
						estadoES = tabelaDeProcessos[bloqueado].atualizaES();
					//saida.println(bloqueado);

					if(estadoES == Estado.Pronto && bloqueado != proximo)
					{
						bloqueados.remove(i);
						i--; //Garante que bloqueado removido nao quebre ordem de iteracao
						prontos.add(bloqueado);
						saida.println("E/S iniciada em " + tabelaDeProcessos[bloqueado].nome);

					}
				}

			}

			saida.close();
			writer.close();
		}
		catch(IOException e)
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
	public String nome;
	int programCounter = 0;
	Estado estado;
	String x = "0";
	String y = "0";
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
		tempoDeEspera--;
		//System.out.println(nome);
		if(tempoDeEspera <= 0)
		{
			terminouES = true;
			estado = Estado.Pronto;
		}
		else
			estado = Estado.Bloqueado;

		return estado;
	}

	public Estado roda()
	{
		estado = Estado.Rodando;
		String comando = comandos.get(programCounter);
		//System.out.println(comando);

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
		if(comando.startsWith("X") || comando.startsWith("Y"))
		{
			String[] a = comando.split("=");
			if(a[0].equals("X"))
				x = a[1];
			else
				y = a[1];
		}

		programCounter++;
		return estado;
	}

	public void preempsao()
	{
		estado = Estado.Pronto;
	}
}
